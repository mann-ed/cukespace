package com.github.cukespace.shared;

public final class ClassLoaders {
    private ClassLoaders() {
        // no-op
    }

    public static Class<?> load(final String name) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(name);
    }
}
