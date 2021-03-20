package io.cucumber.arquillian.junit;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.JUnitClassRulesFilter;
import org.jboss.arquillian.junit.container.ContainerClassRulesFilter;
import org.jboss.arquillian.junit.container.JUnitRemoteExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 18, 2021
 */
public class CukeSpaceDeploymentAppender extends CachedAuxilliaryArchiveAppender {
    @Override
    protected Archive<?> buildArchive() {
        return ShrinkWrap.create(JavaArchive.class, "arquillian-junit.jar")
                .addPackages(true, "junit", "org.junit", "org.hamcrest", Arquillian.class.getPackage().getName())
                .addAsServiceProvider(TestRunner.class, CukespaceTestRunner.class).addClass(JUnitRemoteExtension.class)
                .addAsServiceProvider(RemoteLoadableExtension.class, JUnitRemoteExtension.class)
                .addAsServiceProvider(JUnitClassRulesFilter.class, ContainerClassRulesFilter.class);
    }
}