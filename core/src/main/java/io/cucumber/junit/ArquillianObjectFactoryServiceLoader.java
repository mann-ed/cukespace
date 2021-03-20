package io.cucumber.junit;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Options;
import io.cucumber.core.exception.CucumberException;

public final class ArquillianObjectFactoryServiceLoader {

    private final Supplier<ClassLoader> classLoaderSupplier;
    private final Options               options;

    @Deprecated
    public ArquillianObjectFactoryServiceLoader(final Options options) {
        this(currentThread()::getContextClassLoader, options);
    }

    public ArquillianObjectFactoryServiceLoader(final Supplier<ClassLoader> classLoaderSupplier,
            final Options options) {
        this.classLoaderSupplier = requireNonNull(classLoaderSupplier);
        this.options = requireNonNull(options);
    }

    /**
     * Loads an instance of {@link ObjectFactory} using the {@link ServiceLoader}
     * mechanism.
     * <p>
     * Will load an instance of the class provided by
     * {@link Options#getObjectFactoryClass()}. If
     * {@link Options#getObjectFactoryClass()} does not provide a class and there is
     * exactly one {@code ObjectFactory} instance available that instance will be
     * used.
     * <p>
     * Otherwise {@link DefaultJavaObjectFactory} with no dependency injection
     * capabilities will be used.
     *
     * @return an instance of {@link ObjectFactory}
     */
    ObjectFactory loadObjectFactory() {
        final Class<? extends ObjectFactory> objectFactoryClass = options.getObjectFactoryClass();
        final ClassLoader                    classLoader        = classLoaderSupplier.get();
        final ServiceLoader<ObjectFactory>   loader             = ServiceLoader.load(ObjectFactory.class, classLoader);
        if (objectFactoryClass == null) {
            return loadSingleObjectFactoryOrDefault(loader);

        }

        return loadSelectedObjectFactory(loader, objectFactoryClass);
    }

    private static ObjectFactory loadSingleObjectFactoryOrDefault(final ServiceLoader<ObjectFactory> loader) {
        final Iterator<ObjectFactory> objectFactories = loader.iterator();

        ObjectFactory                 objectFactory;
        if (objectFactories.hasNext()) {
            objectFactory = objectFactories.next();
        } else {
            objectFactory = new DefaultJavaObjectFactory();
        }

        if (objectFactories.hasNext()) {
            final ObjectFactory extraObjectFactory = objectFactories.next();
            throw new CucumberException(getMultipleObjectFactoryLogMessage(objectFactory, extraObjectFactory));
        }
        return objectFactory;
    }

    private static ObjectFactory loadSelectedObjectFactory(final ServiceLoader<ObjectFactory> loader,
            final Class<? extends ObjectFactory> objectFactoryClass) {
        for (final ObjectFactory objectFactory : loader) {
            if (objectFactoryClass.equals(objectFactory.getClass())) {
                return objectFactory;
            }
        }

        throw new CucumberException("" + "Could not find object factory " + objectFactoryClass.getName() + ".\n"
                + "Cucumber uses SPI to discover object factory implementations.\n"
                + "Has the class been registered with SPI and is it available on the classpath?");
    }

    private static String getMultipleObjectFactoryLogMessage(final ObjectFactory... objectFactories) {
        final String factoryNames = Stream.of(objectFactories).map(Object::getClass).map(Class::getName)
                .collect(Collectors.joining(", "));

        return "More than one Cucumber ObjectFactory was found in the classpath\n" + "\n" + "Found: " + factoryNames
                + "\n" + "\n" + "You may have included, for instance, cucumber-spring AND cucumber-guice as part of\n"
                + "your dependencies. When this happens, Cucumber can't decide which to use.\n"
                + "In order to enjoy dependency injection features, either remove the unnecessary dependencies"
                + "from your classpath or use the `cucumber.object-factory` property or `@CucumberOptions(objectFactory=...)` to select one.\n";
    }

    /**
     * Creates glue instances. Does not provide Dependency Injection.
     * <p>
     * All glue classes must have a public no-argument constructor.
     */
    static class DefaultJavaObjectFactory implements ObjectFactory {

        private final Map<Class<?>, Object> instances = new HashMap<>();
        private final CDI<Object>           container = CDI.current();
        ClassLoader                         creationalContextProducer;

        @Override
        public void start() {
            // No-op
        }

        @Override
        public void stop() {
            instances.clear();
        }

        @Override
        public boolean addClass(final Class<?> clazz) {
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getInstance(final Class<T> type) {
            final BeanManager          beanManager       = container.getBeanManager();
            final Set<Bean<?>>         beans             = beanManager.getBeans(type);
            final Bean<?>              bean              = beanManager.resolve(beans);
            final CreationalContext<?> creationalContext = beanManager.createCreationalContext(null);
            try {
                if (!instances.containsKey(type)) {
                    final T typesFound = type.cast(beanManager.getReference(bean, type, creationalContext));
                    instances.put(type, typesFound);
                }
                return (T) instances.get(type);
            } finally {
                creationalContext.release();
            }
        }

    }

}
