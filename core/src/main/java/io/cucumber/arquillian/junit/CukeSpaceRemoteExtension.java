package io.cucumber.arquillian.junit;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.junit.RulesEnricher;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 18, 2021
 */
public class CukeSpaceRemoteExtension implements RemoteLoadableExtension {

    @Override
    public void register(final ExtensionBuilder builder) {
        builder.observer(RulesEnricher.class);
    }
}