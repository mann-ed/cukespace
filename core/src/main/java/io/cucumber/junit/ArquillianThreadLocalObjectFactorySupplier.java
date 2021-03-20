package io.cucumber.junit;

import static java.util.Objects.requireNonNull;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.runtime.ObjectFactorySupplier;

public final class ArquillianThreadLocalObjectFactorySupplier implements ObjectFactorySupplier {

    private final ObjectFactory runners;

    public ArquillianThreadLocalObjectFactorySupplier(
            final ArquillianObjectFactoryServiceLoader objectFactoryServiceLoader) {
        this.runners = requireNonNull(objectFactoryServiceLoader).loadObjectFactory(); // ::loadObjectFactory;
    }

    /*
     * (non-Javadoc)
     *
     * @see io.cucumber.core.runtime.ObjectFactorySupplier#get()
     */
    @Override
    public ObjectFactory get() {
        return this.runners;
    }

}
