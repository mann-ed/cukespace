package com.github.cukespace.core;

import java.util.Collection;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.test.impl.client.deployment.DeploymentGenerator;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.event.suite.Before;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 23, 2021
 */
public class StepEnricherProvider {
    @Inject
    private Instance<ServiceLoader>                            serviceLoader;

    @Inject
    private BeanManager                                        beanManager;

    private static final ThreadLocal<Collection<TestEnricher>> enrichers = new ThreadLocal<>();

    /**
     * Observe the {@link Before} event to obtain references to the
     * {@link TestEnricher} instances. Once the enrichers have been obtained,
     * they're stored in a ThreadLocal instance for future reference.
     *
     * @param event The event to observe
     * @throws Exception The exception thrown by the observer on failure.
     */
    public void enrich(@Observes final Before event) throws Exception {
        final Collection<TestEnricher> testEnrichers = serviceLoader.get().all(TestEnricher.class);
        enrichers.set(testEnrichers);
    }

    public void callback(@Observes(precedence = -1) final EventContext<DeployDeployment> eventContext,
            final TestClass testClass) throws Exception {
        System.out.println(eventContext.getEvent().getDeployment().getDescription().getArchive().getName());
        System.out.println(eventContext.getEvent().getDeployment().getDescription().getArchive().toString(true));
        eventContext.proceed();
    }

    public static Collection<TestEnricher> getEnrichers() {
        return enrichers.get();
    }

    public void unload() {
        enrichers.remove();
    }

}
