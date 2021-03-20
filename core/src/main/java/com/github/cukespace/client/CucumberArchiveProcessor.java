package com.github.cukespace.client;

import static com.github.cukespace.locator.JarLocation.jarLocation;
import static com.github.cukespace.shared.ClassLoaders.load;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.filter.IncludeRegExpPaths;

import com.fasterxml.jackson.annotation.JsonFormat.Features;
import com.github.cukespace.config.CucumberConfiguration;
import com.github.cukespace.container.ContextualObjectFactoryBase;
import com.github.cukespace.container.CucumberContainerExtension;
import com.github.cukespace.lifecycle.CucumberLifecycle;
import com.github.cukespace.shared.ClientServerFiles;
import com.sun.jdi.event.StepEvent;

import io.cucumber.arquillian.junit.ArquillianCucumber;
import io.cucumber.junit.BaseCukeSpace;
import io.cucumber.junit.CukeSpace;

public class CucumberArchiveProcessor implements ApplicationArchiveProcessor {
    private static volatile StringAsset     scannedAnnotations = null;

    @Inject
    private Instance<CucumberConfiguration> configuration;

    @Override
    public void process(final Archive<?> applicationArchive, final TestClass testClass) {
        if (JavaArchive.class.isInstance(applicationArchive)) {
            return;
        }

        // try to find the feature
        final Class<?>    javaClass  = testClass.getJavaClass();
        final ClassLoader loader     = Thread.currentThread().getContextClassLoader();

        Class             testNgBase = null;
        try {
            testNgBase = loader.loadClass("cucumber.runtime.arquillian.testng.CukeSpace");
        } catch (final ClassNotFoundException e) {
            // no-op
        } catch (final NoClassDefFoundError e) {
            // no-op
        }
        final boolean               junit                 = testNgBase == null
                || !testNgBase.isAssignableFrom(javaClass);

        final String                ln                    = System.getProperty("line.separator");

        final LibraryContainer<?>   libraryContainer      = (LibraryContainer<?>) applicationArchive;

        // add feature file + list of annotations
        final JavaArchive           resourceJar           = create(JavaArchive.class, "cukespace-resources.jar");

        final CucumberConfiguration cucumberConfiguration = configuration.get();
        final boolean               report                = cucumberConfiguration.isReport()
                || cucumberConfiguration.isGenerateDocs();
        final String                reportDirectory       = cucumberConfiguration.getReportDirectory();

        addCucumberAnnotations(ln, resourceJar);
        addConfiguration(resourceJar, cucumberConfiguration, report, reportDirectory);

        libraryContainer.addAsLibrary(resourceJar);

        // cucumber-java and cucumber-core
        enrichWithDefaultCucumber(libraryContainer);

        final LibraryContainer<?> entryPointContainer = (LibraryContainer<?>) findArchiveByTestClass(applicationArchive,
                testClass.getJavaClass());

        // glues
        enrichWithGlues(javaClass, entryPointContainer, ln);

        // cucumber-arquillian
        enrichWithCukeSpace(entryPointContainer, junit);

        // if scala module is available at classpath
        final Set<ArchivePath> libs = applicationArchive.getContent(new IncludeRegExpPaths("/WEB-INF/lib/.*jar"))
                .keySet();
        tryToAdd(libs, libraryContainer, "WEB-INF/lib/scala-library-", "cucumber.api.scala.ScalaDsl", "scala.App");
    }

    protected final Archive<? extends Archive<?>> findArchiveByTestClass(final Archive<?> topArchive,
            final Class testClass) {
        Archive<?> testArchive = topArchive;

        if (!archiveContains(testArchive, testClass)) {
            for (final Node node : testArchive.getContent(Filters.include(".*\\.(jar|war)")).values()) {
                if (node.getAsset() instanceof ArchiveAsset) {
                    final Archive archive = ((ArchiveAsset) node.getAsset()).getArchive();

                    if (archiveContains(archive, testClass) && archive instanceof LibraryContainer) {
                        testArchive = archive;
                    }
                }
            }
        }

        return testArchive;
    }

    private boolean archiveContains(final Archive<?> archive, final Class<?> clazz) {
        final String classPath    = clazz.getCanonicalName().replace('.', '/') + ".class";
        final String warClassPath = "WEB-INF/classes/" + classPath;

        return archive.contains(classPath) || archive.contains(warClassPath);
    }

    private static void addConfiguration(final JavaArchive resourceJar,
            final CucumberConfiguration cucumberConfiguration, final boolean report, final String reportDirectory) {
        final StringBuilder config = new StringBuilder();
        config.append(CucumberConfiguration.COLORS).append("=").append(cucumberConfiguration.isColorized()).append("\n")
                .append(CucumberConfiguration.REPORTABLE).append("=").append(report).append("\n")
                .append(CucumberConfiguration.REPORTABLE_PATH).append("=")
                .append(reportDirectory == null ? null : new File(reportDirectory).getAbsolutePath()).append("\n");

        if (cucumberConfiguration.getObjectFactory() != null) {
            config.append(CucumberConfiguration.OBJECT_FACTORY).append("=")
                    .append(cucumberConfiguration.getObjectFactory()).append("\n");
        }
        if (cucumberConfiguration.hasOptions()) {
            config.append(CucumberConfiguration.OPTIONS).append("=").append(cucumberConfiguration.getOptions());
        }

        // resourceJar.addAsResource(new StringAsset(config.toString()),
        // ClientServerFiles.CONFIG);
    }

    private static void addCucumberAnnotations(final String ln, final JavaArchive resourceJar) {
        if (scannedAnnotations == null) {
            synchronized (CucumberArchiveProcessor.class) {
                if (scannedAnnotations == null) {
                    final StringBuilder builder = new StringBuilder();
                    for (final Class<? extends Annotation> annotation : CucumberLifecycle.cucumberAnnotations()) {
                        builder.append(annotation.getName()).append(ln);
                    }
                    scannedAnnotations = new StringAsset(builder.toString());
                }
            }
        }
        resourceJar.addAsResource(scannedAnnotations, ClientServerFiles.ANNOTATION_LIST);
    }

    private static void enrichWithDefaultCucumber(final LibraryContainer<?> libraryContainer) {
        System.out.println("enrichWithDefaultCucumber");
        /*
         * libraryContainer.addAsLibraries(jarLocation(Mapper.class),
         * jarLocation(ResourceLoaderClassFinder.class),
         * jarLocation(ConverterRegistry.class), jarLocation(JavaBackend.class)); for
         * (final String potential : asList("cucumber.api.junit.Cucumber",
         * "cucumber.api.testng.TestNGCucumberRunner",
         * "cucumber.runtime.java8.LambdaGlueBase")) { try {
         * libraryContainer.addAsLibraries(
         * jarLocation(Thread.currentThread().getContextClassLoader().loadClass(
         * potential))); } catch (final Throwable e) { // no-op } }
         */
    }

    private static void enrichWithGlues(final Class<?> javaClass, final LibraryContainer<?> libraryContainer,
            final String ln) {
        System.out.println("enrichWithGlues");
        /*
         * final Collection<Class<?>> glues = Glues.findGlues(javaClass); final
         * StringBuilder gluesStr = new StringBuilder(); if (!glues.isEmpty()) { final
         * JavaArchive gluesJar = create(JavaArchive.class, "cukespace-glues.jar"); { //
         * glues txt file for (final Class<?> g : glues) {
         * gluesStr.append(g.getName()).append(ln); } gluesJar.add(new
         * StringAsset(gluesStr.toString()), ClientServerFiles.GLUES_LIST); }
         *
         * { // classes gluesJar.addClasses(glues.toArray(new Class<?>[glues.size()]));
         * for (final Class<?> clazz : glues) { Class<?> current =
         * clazz.getSuperclass(); while (!Object.class.equals(current)) { if
         * (!gluesJar.contains(AssetUtil.getFullPathForClassResource(current))) {
         * gluesJar.addClass(current); } current = current.getSuperclass(); } } }
         *
         * libraryContainer.addAsLibrary(gluesJar); }
         */
    }

    private static void enrichWithCukeSpace(final LibraryContainer<?> libraryContainer, final boolean junit) {
        final JavaArchive archive = create(JavaArchive.class, "cukespace-core.jar")
                .addAsServiceProvider(RemoteLoadableExtension.class, CucumberContainerExtension.class)
                // .addPackage(cucumber.runtime.arquillian.api.Glues.class.getPackage())
                .addPackage(StepEvent.class.getPackage()).addClasses(CucumberLifecycle.class, BaseCukeSpace.class)
                .addClasses(CucumberConfiguration.class, CucumberContainerExtension.class, Features.class,
                        ContextualObjectFactoryBase.class);
        // .addPackage(ClientServerFiles.class.getPackage());

        if (junit) {
            archive.addClasses(ArquillianCucumber.class, CukeSpace.class,
                    ArquillianCucumber.InstanceControlledFrameworkMethod.class);
        } else {
            /*
             * archive.addClasses(cucumber.runtime.arquillian.testng.CukeSpace.class,
             * cucumber.runtime.arquillian.testng.CukeSpace.TestNGCukeSpace.class,
             * cucumber.runtime.arquillian.testng.CukeSpace.FormaterReporterFacade.class);
             */
        }
        libraryContainer.addAsLibrary(archive);
    }

    private static void tryToAdd(final Collection<ArchivePath> paths, final LibraryContainer<?> container,
            final String exclusion, final String... classes) {
        final Collection<File> files = new ArrayList<File>();

        try { // if scala dsl is here, add it
            for (final String clazz : classes) {
                final File file  = jarLocation(load(clazz));

                boolean    found = false;
                for (final ArchivePath ap : paths) {
                    final String path = ap.get();
                    if (path.contains(exclusion) && path.endsWith(".jar")) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    files.add(file);
                }
            }
        } catch (final Exception e) {
            return; // if any jar is missing don't add it
        }

        container.addAsLibraries(files.toArray(new File[files.size()]));
    }

    private static String featureName(final URL url) {
        // file
        final File f = new File(url.getFile());
        if (f.exists()) {
            return f.getName();
        }

        // classpath
        final String path = url.getPath();
        if (path.lastIndexOf("!") < path.lastIndexOf("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }

        // fallback
        return Math.abs(url.hashCode()) + "ed";
    }
}