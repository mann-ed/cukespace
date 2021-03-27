package com.github.cukespace.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

// base class to ease custom lookups of steps
public class CukeSpaceCDIObjectFactory extends ContextualObjectFactoryBase {
    private final Collection<CreationalContext<?>> contexts  = new ArrayList<>();
    private final Map<Class<?>, Object>            instances = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getInstance(final Class<T> glueClass) {
        final BeanManager               beanManager       = CDI.current().getBeanManager();
        final Bean<?>                   bean              = beanManager.resolve(beanManager.getBeans(glueClass));
        final CreationalContext<Object> creationalContext = beanManager.createCreationalContext(null);
        if (!beanManager.isNormalScope(bean.getScope())) {
            contexts.add(creationalContext);
        }
        try {
            if (!instances.containsKey(glueClass)) {
                final T typesFound = glueClass.cast(beanManager.getReference(bean, glueClass, creationalContext));
                instances.put(glueClass, typesFound);
            }
            return (T) instances.get(glueClass);
        } finally {
            creationalContext.release();
        }
    }

    @Override
    public void stop() {
        for (final CreationalContext<?> cc : contexts) {
            cc.release();
        }
        instances.clear();
        contexts.clear();
    }
}