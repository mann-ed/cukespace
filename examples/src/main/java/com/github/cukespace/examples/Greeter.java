package com.github.cukespace.examples;

import java.io.PrintStream;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 18, 2021
 */
public class Greeter {

    public void greet(final PrintStream to, final String name) {
        to.println(createGreeting(name));
    }

    public String createGreeting(final String name) {
        return "Hello, " + name + "!";
    }
}
