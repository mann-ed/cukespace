package io.cucumber.junit;

import org.junit.runners.model.InitializationError;

import io.cucumber.arquillian.junit.ArquillianCucumber;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 17, 2021
 */
public class CukeSpace extends ArquillianCucumber {

    public CukeSpace(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

}
