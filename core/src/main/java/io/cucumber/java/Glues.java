package io.cucumber.java;

import static java.lang.Thread.currentThread;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.cukespace.container.CukeSpaceCDIObjectFactory;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.cucumberexpressions.DuplicateTypeNameException;
import io.cucumber.junit.CucumberOptions;

public final class Glues {
    private Glues() {
        // no-op
    }

    public static Collection<Class<?>> findGlues(final Class<?> clazz) {
        final Collection<Class<?>> glues           = new ArrayList<>();

        /*
         * final io.cucumber.core.backend.Glue additionalGlues =
         * clazz.getAnnotation(io.cucumber.core.backend.Glue.class); if (additionalGlues
         * != null) { Collections.addAll(glues, additionalGlues.value()); }
         */
        final ObjectFactory        factory         = new CukeSpaceCDIObjectFactory();
        final CucumberOptions      cucumberOptions = clazz.getAnnotation(CucumberOptions.class);
        if (cucumberOptions != null) {
            final List<URI> cucumberGlues = Arrays.asList(cucumberOptions.glue()).stream()
                    .map(c -> URI.create("classpath:" + c)).collect(Collectors.toList());
            if (!cucumberGlues.isEmpty()) {
                final JavaBackend javaBackend = new JavaBackend(factory, factory,
                        currentThread()::getContextClassLoader);
                final ScanGlue    glue        = new ScanGlue();
                javaBackend.loadGlue(glue, cucumberGlues);

                glues.addAll(glue.classes);
            }
        }

        return glues;
    }

    private static class ScanGlue implements Glue {
        private final Set<Class<?>> classes = new HashSet<>(); // make classes unique

        private static Class<?> clazz(final Object hookDefinition) {
            final Class<?> stepClass = hookDefinition.getClass();
            if (stepClass.getName().startsWith("io.cucumber.java.JavaStepDefinition")) {
                try {
                    final AbstractGlueDefinition def = (AbstractGlueDefinition) hookDefinition;

                    // final Field f = stepClass.getDeclaredField("method");
                    final Method                 m   = def.method;
                    return m.getDeclaringClass();
                } catch (final Exception e) {
                    // no-op
                    System.out.println("ERROR IS :" + e.getMessage());
                }
            }
            return null;
        }

        private void addClazz(final Class<?> clazz) {
            if (clazz != null) {
                classes.add(clazz);
            }
        }

        @Override
        public void addStepDefinition(final StepDefinition stepDefinition) throws DuplicateTypeNameException {
            addClazz(clazz(stepDefinition));
        }

        @Override
        public void addBeforeHook(final HookDefinition hookDefinition) {
            addClazz(clazz(hookDefinition));
        }

        @Override
        public void addAfterHook(final HookDefinition hookDefinition) {
            addClazz(clazz(hookDefinition));
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * io.cucumber.core.backend.Glue#addBeforeStepHook(io.cucumber.core.backend.
         * HookDefinition)
         */
        @Override
        public void addBeforeStepHook(final HookDefinition beforeStepHook) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see io.cucumber.core.backend.Glue#addAfterStepHook(io.cucumber.core.backend.
         * HookDefinition)
         */
        @Override
        public void addAfterStepHook(final HookDefinition afterStepHook) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see io.cucumber.core.backend.Glue#addParameterType(io.cucumber.core.backend.
         * ParameterTypeDefinition)
         */
        @Override
        public void addParameterType(final ParameterTypeDefinition parameterType) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see io.cucumber.core.backend.Glue#addDataTableType(io.cucumber.core.backend.
         * DataTableTypeDefinition)
         */
        @Override
        public void addDataTableType(final DataTableTypeDefinition dataTableType) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see
         * io.cucumber.core.backend.Glue#addDefaultParameterTransformer(io.cucumber.core
         * .backend.DefaultParameterTransformerDefinition)
         */
        @Override
        public void addDefaultParameterTransformer(
                final DefaultParameterTransformerDefinition defaultParameterTransformer) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see
         * io.cucumber.core.backend.Glue#addDefaultDataTableEntryTransformer(io.cucumber
         * .core.backend.DefaultDataTableEntryTransformerDefinition)
         */
        @Override
        public void addDefaultDataTableEntryTransformer(
                final DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see
         * io.cucumber.core.backend.Glue#addDefaultDataTableCellTransformer(io.cucumber.
         * core.backend.DefaultDataTableCellTransformerDefinition)
         */
        @Override
        public void addDefaultDataTableCellTransformer(
                final DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer) {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         *
         * @see io.cucumber.core.backend.Glue#addDocStringType(io.cucumber.core.backend.
         * DocStringTypeDefinition)
         */
        @Override
        public void addDocStringType(final DocStringTypeDefinition docStringType) {
            // TODO Auto-generated method stub

        }

    }
}