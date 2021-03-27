package com.github.cukespace.container.cdi;

import java.util.Map;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import com.github.cukespace.client.CucumberArchiveProcessor;
import com.github.cukespace.config.CucumberConfiguration;
import com.github.cukespace.container.CukeSpaceCDIObjectFactory;

public class CDIArchiveProcessor extends CucumberArchiveProcessor {

    @Inject
    private Instance<CucumberConfiguration> configuration;

    @Override
    public void process(final Archive<?> applicationArchive, final TestClass testClass) {
        super.process(applicationArchive, testClass);

        final CucumberConfiguration cucumberConfiguration = configuration.get();

        final boolean               cdiEnabled            = cucumberConfiguration.getObjectFactory() != null
                && CukeSpaceCDIObjectFactory.class.getName()
                        .equalsIgnoreCase(cucumberConfiguration.getObjectFactory().trim());

        if (cdiEnabled) {
            enrichWithCDI(findArchiveByTestClass(applicationArchive, testClass.getJavaClass()));
        }
    }

    private void enrichWithCDI(final Archive<?> applicationArchive) {
        final Map<ArchivePath, Node> contentMap = applicationArchive
                .getContent(Filters.include(".*/cukespace-core.jar"));
        for (final Node node : contentMap.values()) {
            if (node.getAsset() instanceof ArchiveAsset) {
                final JavaArchive archive = (JavaArchive) ((ArchiveAsset) node.getAsset()).getArchive();

                archive.addClass(CukeSpaceCDIObjectFactory.class);
                archive.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
            }
        }
    }

}