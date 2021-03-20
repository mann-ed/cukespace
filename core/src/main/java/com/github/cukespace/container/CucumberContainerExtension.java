package com.github.cukespace.container;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;

import com.github.cukespace.lifecycle.CucumberLifecycle;
import com.github.cukespace.shared.EventHelper;
import com.github.cukespace.shared.PersistenceExtensionIntegration;

public class CucumberContainerExtension implements RemoteLoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.observer(CucumberLifecycle.class).observer(EventHelper.class);
        if (PersistenceExtensionIntegration.isOn()) {
            builder.observer(PersistenceExtensionIntegration.Observer.class);
        }
    }
}
