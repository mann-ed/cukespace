package com.github.cukespace.client;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

import com.github.cukespace.core.StepEnricherProvider;
import com.github.cukespace.lifecycle.CucumberLifecycle;

public class CucumberClientExtension implements LoadableExtension {

    @Override
    public void register(final ExtensionBuilder builder) {
        builder.service(ApplicationArchiveProcessor.class, CucumberArchiveProcessor.class)
                .observer(CucumberLifecycle.class).observer(CucumberConfigurationProducer.class)
                .observer(StepEnricherProvider.class);

    }
}