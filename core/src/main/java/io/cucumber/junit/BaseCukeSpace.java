package io.cucumber.junit;

import static io.cucumber.junit.FileNameCompatibleNames.uniqueSuffix;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.CucumberExecutionContext;
import io.cucumber.core.runtime.ExitStatus;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.runtime.TypeRegistryConfigurerSupplier;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 19, 2021
 */
public class BaseCukeSpace extends ParentRunner<ParentRunner<?>> {

    private final List<ParentRunner<?>>    children;
    private final EventBus                 bus;
    private final List<Feature>            features;
    private final Plugins                  plugins;
    private final CucumberExecutionContext context;

    private boolean                        multiThreadingAssumed = false;

    /**
     * @param testClass
     * @throws InitializationError
     */
    public BaseCukeSpace(final Class<?> clazz) throws InitializationError {
        super(clazz);
        Assertions.assertNoCucumberAnnotatedMethods(clazz);

        // Parse the options early to provide fast feedback about invalid
        // options
        final RuntimeOptions propertiesFileOptions      = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromPropertiesFile()).build();

        final RuntimeOptions annotationOptions          = new CucumberOptionsAnnotationParser()
                .withOptionsProvider(new JUnitCucumberOptionsProvider()).parse(clazz).build(propertiesFileOptions);

        final RuntimeOptions environmentOptions         = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromEnvironment()).build(annotationOptions);

        final RuntimeOptions runtimeOptions             = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromSystemProperties()).enablePublishPlugin().build(environmentOptions);

        // Next parse the junit options
        final JUnitOptions   junitPropertiesFileOptions = new JUnitOptionsParser()
                .parse(CucumberProperties.fromPropertiesFile()).build();

        final JUnitOptions   junitAnnotationOptions     = new JUnitOptionsParser().parse(clazz)
                .build(junitPropertiesFileOptions);

        final JUnitOptions   junitEnvironmentOptions    = new JUnitOptionsParser()
                .parse(CucumberProperties.fromEnvironment()).build(junitAnnotationOptions);

        final JUnitOptions   junitOptions               = new JUnitOptionsParser()
                .parse(CucumberProperties.fromSystemProperties()).build(junitEnvironmentOptions);

        this.bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

        // Parse the features early. Don't proceed when there are lexer errors
        final FeatureParser              parser          = new FeatureParser(bus::generateId);
        final Supplier<ClassLoader>      classLoader     = this::getCukeSpaceClassLoader;
        final FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(classLoader, runtimeOptions,
                parser);
        this.features = featureSupplier.get();
        // Create plugins after feature parsing to avoid the creation of empty
        // files on lexer errors.
        this.plugins = new Plugins(new PluginFactory(), runtimeOptions);
        final ExitStatus exitStatus = new ExitStatus(runtimeOptions);
        this.plugins.addPlugin(exitStatus);

        final ArquillianObjectFactoryServiceLoader objectFactoryServiceLoader     = new ArquillianObjectFactoryServiceLoader(
                classLoader, runtimeOptions);
        final ObjectFactorySupplier                objectFactorySupplier          = new ArquillianThreadLocalObjectFactorySupplier(
                objectFactoryServiceLoader);
        final BackendSupplier                      backendSupplier                = new BackendServiceLoader(
                clazz::getClassLoader, objectFactorySupplier);
        final TypeRegistryConfigurerSupplier       typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(
                classLoader, runtimeOptions);
        final ThreadLocalRunnerSupplier            runnerSupplier                 = new ThreadLocalRunnerSupplier(
                runtimeOptions, bus, backendSupplier, objectFactorySupplier, typeRegistryConfigurerSupplier);
        this.context = new CucumberExecutionContext(bus, exitStatus, runnerSupplier);
        final Predicate<Pickle>                    filters       = new Filters(runtimeOptions);

        final Map<Optional<String>, List<Feature>> groupedByName = features.stream()
                .collect(groupingBy(Feature::getName));

        this.children = features.stream().map(feature -> {
            final Integer uniqueSuffix = uniqueSuffix(groupedByName, feature, Feature::getName);

            // return Request.runner(CukeRunners.create(feature, uniqueSuffix, filters,
            // runnerSupplier, junitOptions));
            return CukeRunners.create(feature, uniqueSuffix, filters, runnerSupplier, junitOptions);
        }).filter(runner -> !runner.isEmpty()).collect(toList());

    }

    /**
     * @return the beanManagerInst
     */
    public BeanManager getBeanManager() {
        return CDI.current().getBeanManager();
    }

    protected ClassLoader getCukeSpaceClassLoader() {
        return CDI.current().getBeanManager().getClass().getClassLoader();
    }

    @Override
    protected List<ParentRunner<?>> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(final ParentRunner<?> child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(final ParentRunner<?> child, final RunNotifier notifier) {

        child.run(notifier);
    }

    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        final Statement runFeatures = super.childrenInvoker(notifier);
        return new RunCucumber(runFeatures);
    }

    @Override
    public void setScheduler(final RunnerScheduler scheduler) {
        super.setScheduler(scheduler);
        multiThreadingAssumed = true;
    }

    class RunCucumber extends Statement {

        private final Statement runFeatures;

        RunCucumber(final Statement runFeatures) {
            this.runFeatures = runFeatures;
        }

        @Override
        public void evaluate() throws Throwable {
            if (multiThreadingAssumed) {
                plugins.setSerialEventBusOnEventListenerPlugins(bus);
            } else {
                plugins.setEventBusOnEventListenerPlugins(bus);
            }

            context.startTestRun();
            features.forEach(context::beforeFeature);

            try {
                runFeatures.evaluate();
            } finally {
                context.finishTestRun();
            }
        }

    }
}
