package com.github.cukespace.container;

import io.cucumber.core.backend.ObjectFactory;

//base class to ease custom lookups of steps
public abstract class ContextualObjectFactoryBase implements ObjectFactory {
    @Override
    public void start() {
        // no-op
    }

    @Override
    public void stop() {
        // no-op
    }

    @Override
    public boolean addClass(final Class<?> glueClass) {
        return true;
    }
}