package com.github.cukespace.container.cdi;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

import io.cucumber.java.CucumberArchiveProcessor;

public class CDIExtension implements LoadableExtension {

    @Override
    public void register(final ExtensionBuilder builder) {
        builder.override(ApplicationArchiveProcessor.class, CucumberArchiveProcessor.class, CDIArchiveProcessor.class)
                .observer(CDIConfigurationObserver.class);
    }

}