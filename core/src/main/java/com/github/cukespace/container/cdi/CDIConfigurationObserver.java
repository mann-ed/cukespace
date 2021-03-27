package com.github.cukespace.container.cdi;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

import com.github.cukespace.config.CucumberConfiguration;
import com.github.cukespace.container.CukeSpaceCDIObjectFactory;

public class CDIConfigurationObserver {
    @Inject
    private Instance<CucumberConfiguration> configuration;

    public void findConfiguration(final @Observes(precedence = -10) ArquillianDescriptor descriptor) {
        final CucumberConfiguration cucumberConfiguration = configuration.get();
        final String                objectFactory         = cucumberConfiguration.getObjectFactory();

        if ("cdi".equalsIgnoreCase(objectFactory)) {
            cucumberConfiguration.setObjectFactory(CukeSpaceCDIObjectFactory.class.getCanonicalName());
        }
    }
}