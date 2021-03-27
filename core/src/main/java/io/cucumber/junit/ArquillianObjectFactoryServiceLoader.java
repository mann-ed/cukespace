package io.cucumber.junit;

import static java.lang.Thread.currentThread;

import java.util.function.Supplier;

import com.github.cukespace.container.CukeSpaceCDIObjectFactory;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Options;

public final class ArquillianObjectFactoryServiceLoader {

    @Deprecated
    public ArquillianObjectFactoryServiceLoader(final Options options) {
        this(currentThread()::getContextClassLoader, options);
    }

    public ArquillianObjectFactoryServiceLoader(final Supplier<ClassLoader> classLoaderSupplier,
            final Options options) {

    }

    /**
     * Just using our CukeSpaceCDIObjectFactory.
     *
     * @return an instance of {@link ObjectFactory}
     */
    ObjectFactory loadObjectFactory() {
        return new CukeSpaceCDIObjectFactory();
    }

}
