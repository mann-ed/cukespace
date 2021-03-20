package com.github.cukespace.container;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

import com.github.cukespace.config.CucumberConfiguration;

public class CDIConfigurationObserver {
    @Inject
    private Instance<CucumberConfiguration> configuration;

    @Inject
    private Instance<BeanManager>           beanManagerInst;

    public void findConfiguration(final @Observes(precedence = -10) ArquillianDescriptor descriptor) {
        final CucumberConfiguration cucumberConfiguration = configuration.get();
        final String                objectFactory         = cucumberConfiguration.getObjectFactory();

        if ("cdi".equalsIgnoreCase(objectFactory) || null != beanManagerInst) {
            cucumberConfiguration.setObjectFactory(CukeSpaceCDIObjectFactory.class.getCanonicalName());
        }
    }
}