package com.github.cukespace.client;

import org.jboss.arquillian.core.spi.LoadableExtension;

public class CucumberClientExtension implements LoadableExtension {

    @Override
    public void register(final ExtensionBuilder builder) {
        // builder.service(ApplicationArchiveProcessor.class,
        // CucumberArchiveProcessor.class);
        // builder.observer(CucumberLifecycle.class);
        builder.observer(CucumberConfigurationProducer.class);
        // builder.observer(StepEnricherProvider.class);

    }
}