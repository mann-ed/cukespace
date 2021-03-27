package com.github.cukespace.stream;

import java.io.PrintStream;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 22, 2021
 */
public class NotCloseablePrintStream extends PrintStream {
    public NotCloseablePrintStream(final PrintStream originalOut) {
        super(originalOut);
    }

    @Override
    public void close() {
        flush();
    }
}
