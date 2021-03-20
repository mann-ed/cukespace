package io.cucumber.arquillian.junit;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *<p>
 * Created: Mar 18, 2021
 */
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.junit.container.JUnitDeploymentAppender;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CukeSpaceContainerExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.service(AuxiliaryArchiveAppender.class, JUnitDeploymentAppender.class);
    }
}
