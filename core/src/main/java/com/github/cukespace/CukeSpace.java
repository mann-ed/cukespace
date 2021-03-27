package com.github.cukespace;

import org.junit.runners.model.InitializationError;

import io.cucumber.junit.ArquillianCucumber;

// just an alias
public class CukeSpace extends ArquillianCucumber {

    public CukeSpace(final Class<?> testClass) throws InitializationError {
        super(testClass);
    }

}
