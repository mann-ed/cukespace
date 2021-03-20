package com.github.cukespace.lifecycle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

import io.cucumber.java.en.And;
import io.cucumber.java.en.But;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * These observers are for: 1. get cucumber annotations from a client scan
 * (avoid server scan hacks/specific modules) -&gt; it is done as late as
 * possible to be able a good TCCL even in embedded mode 2. hack System.out for
 * IDE integration 3. get test enricher to enrich additional glues
 */
public class CucumberLifecycle {
    private static final PrintStream                             ORIGINAL_OUT         = System.out;
    private static final PrintStream                             NOT_CLOSEABLE_OUT    = new PrintStream(ORIGINAL_OUT);
    private static final Collection<Class<? extends Annotation>> CUCUMBER_ANNOTATIONS = new ArrayList<Class<? extends Annotation>>();
    private static final Collection<TestEnricher>                TEST_ENRICHERS       = new ArrayList<TestEnricher>();
    private static volatile Collection<ResourceLoader>           RESOURCES_LOADERS    = null;

    @Inject
    private Instance<ServiceLoader>                              serviceLoader;

    public void init(final @Observes BeforeClass beforeClass) {
        System.setOut(NOT_CLOSEABLE_OUT);
    }

    public void reset(final @Observes AfterClass afterClass) {
        System.setOut(ORIGINAL_OUT);
        CUCUMBER_ANNOTATIONS.clear();
        TEST_ENRICHERS.clear();
        RESOURCES_LOADERS = null;
    }

    // do it lazily to get the right classloader + be sure contexts are started
    // (drone...)
    public void loadCucumberAnnotationsAndEnrichers(final @Observes(precedence = 1) Before before) {
        // enrichers - load new enrichers every time because of issue #81
        TEST_ENRICHERS.clear();
        synchronized (TEST_ENRICHERS) {
            if (TEST_ENRICHERS.isEmpty()) {
                TEST_ENRICHERS.addAll(serviceLoader.get().all(TestEnricher.class));
            }
        }

        // cucumber annotations
        if (CUCUMBER_ANNOTATIONS.isEmpty()) { // don't do it N times
            synchronized (CUCUMBER_ANNOTATIONS) {
                if (CUCUMBER_ANNOTATIONS.isEmpty()) { // don't do it N times
                    final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                    final InputStream is   = null;
                    if (is != null) {
                        String line;
                        try {
                            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            while ((line = reader.readLine()) != null) {
                                try {
                                    CUCUMBER_ANNOTATIONS.add((Class<? extends Annotation>) tccl.loadClass(line));
                                } catch (final ClassNotFoundException e) {
                                    // no-op
                                }
                            }
                        } catch (final IOException e) {
                            // no-op
                        } finally {
                            try {
                                is.close();
                            } catch (final IOException e) {
                                // no-op
                            }
                        }
                    }
                }
            }
        }

        // resource loaders
        if (RESOURCES_LOADERS == null) {
            synchronized (this) {
                if (RESOURCES_LOADERS == null) {
                    RESOURCES_LOADERS = new ArrayList<ResourceLoader>();
                    RESOURCES_LOADERS.addAll(serviceLoader.get().all(ResourceLoader.class));
                    for (final ResourceLoader l : java.util.ServiceLoader.load(ResourceLoader.class)) {
                        RESOURCES_LOADERS.add(l);
                    }
                }
            }
        }
    }

    public static Collection<Class<? extends Annotation>> cucumberAnnotations() {
        if (!CUCUMBER_ANNOTATIONS.isEmpty()) {
            return CUCUMBER_ANNOTATIONS;
        }

        synchronized (CUCUMBER_ANNOTATIONS) {
            if (!CUCUMBER_ANNOTATIONS.isEmpty()) {
                return CUCUMBER_ANNOTATIONS;
            }

            final ClassLoader loader = Thread.currentThread().getContextClassLoader();

            if (CUCUMBER_ANNOTATIONS.isEmpty()) {
                return Arrays.asList(Given.class, When.class, Then.class, And.class, But.class);
            }
            return CUCUMBER_ANNOTATIONS;
        }
    }

    public static Object enrich(final Object instance) {
        for (final TestEnricher enricher : TEST_ENRICHERS) {
            try {
                enricher.enrich(instance);
            } catch (final Exception e) {
                // no-op: don't make all enrichment fail because of one enricher
            }
        }
        return instance;
    }

    public static Collection<ResourceLoader> resourceLoaders() {
        if (RESOURCES_LOADERS == null) { // shouldn't happen
            return Collections.emptyList();
        }
        return RESOURCES_LOADERS;
    }
}