package io.cucumber.arquillian.junit;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import io.cucumber.junit.BaseCukeSpace;

public class ArquillianCucumber extends Arquillian {
    private static final String   RUN_CUCUMBER_MTD = "getCukeTestClass";

    private List<FrameworkMethod> methods;

    @Inject
    private final BaseCukeSpace   delegate;

    public ArquillianCucumber(final Class<?> testClass) throws InitializationError {
        this(testClass, new BaseCukeSpace(testClass));
    }

    public ArquillianCucumber(final Class<?> testClass, final BaseCukeSpace delegate) throws InitializationError {
        super(testClass);
        this.delegate = delegate;
    }

    @Override
    protected Description describeChild(final FrameworkMethod method) {
        if (!Boolean.getBoolean("cukespace.runner.standard-describe")
                && InstanceControlledFrameworkMethod.class.isInstance(method)) {
            return Description.createTestDescription(
                    InstanceControlledFrameworkMethod.class.cast(method).getOriginalClass(),
                    ArquillianCucumber.RUN_CUCUMBER_MTD, method.getAnnotations());
        }
        return super.describeChild(method);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        if (methods != null) {
            return methods;
        }

        methods = new LinkedList<>();

        // run @Test methods
        for (final FrameworkMethod each : ArquillianCucumber.super.computeTestMethods()) {
            methods.add(each);
        }
        // hello ed
        methods.addAll(getChildren());
        try { // run cucumber, this looks like a hack but that's to keep @Before/@After/...
              // hooks behavior
            final Method runCucumber = BaseCukeSpace.class.getDeclaredMethod(RUN_CUCUMBER_MTD, Object.class,
                    Object.class);
            methods.add(new InstanceControlledFrameworkMethod(this, getTestClass().getJavaClass(), runCucumber));
        } catch (final NoSuchMethodException e) {
            // no-op: will not accur...if so this exception is not your biggest issue
        }

        return methods;
    }

    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        if (InstanceControlledFrameworkMethod.class.isInstance(method)) {
            InstanceControlledFrameworkMethod.class.cast(method).setNotifier(notifier);
        }
        // super.runChild(method, notifier);
        this.delegate.run(notifier);
    }

    public static class InstanceControlledFrameworkMethod extends FrameworkMethod {
        private final ArquillianCucumber instance;
        private final Class<?>           originalClass;
        private RunNotifier              notifier;

        private InstanceControlledFrameworkMethod(final ArquillianCucumber runner, final Class<?> originalClass,
                final Method runCucumber) {
            super(runCucumber);
            this.originalClass = originalClass;
            this.instance = runner;
        }

        @Override
        public Object invokeExplosively(final Object target, final Object... params) throws Throwable {
            /*
             * instance.delegate.performInternalCucumberOperations(target, notifier == null
             * ? new RunNotifier() : notifier);
             */
            return null;
        }

        public Class<?> getOriginalClass() {
            return originalClass;
        }

        public void setNotifier(final RunNotifier notifier) {
            this.notifier = notifier;
        }
    }
}