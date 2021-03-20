package io.cucumber.runtime.arquillian.backend;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

final class ArquillianBackend implements Backend {

    private final Container        container;
    private final ClasspathScanner classFinder;

    ArquillianBackend(final Container container, final Supplier<ClassLoader> classLoaderSupplier) {
        this.container = container;
        this.classFinder = new ClasspathScanner(classLoaderSupplier);
    }

    @Override
    public void loadGlue(final Glue glue, final List<URI> gluePaths) {
        gluePaths.stream().filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName).map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream)
                .filter((final Class clazz) -> clazz.getAnnotation(CucumberContextConfiguration.class) != null)
                .forEach(container::addClass);
    }

    @Override
    public void buildWorld() {

    }

    @Override
    public void disposeWorld() {

    }

    @Override
    public Snippet getSnippet() {
        return null;
    }

}
