package io.cucumber.junit;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class ArquillianCucumber extends Arquillian {

    private static final String   RUN_CUCUMBER_MTD = "getCukeTestClass";

    private List<FrameworkMethod> methods;

    private BaseCukeSpace         delegate;

    public ArquillianCucumber(final Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Description describeChild(final FrameworkMethod method) {
        if (!Boolean.getBoolean("cukespace.runner.standard-describe")
                && InstanceControlledFrameworkMethod.class.isInstance(method)) {
            final InstanceControlledFrameworkMethod cukeMethod = InstanceControlledFrameworkMethod.class.cast(method);
            if (null != cukeMethod.getRunner()) {
                return cukeMethod.getRunner().getDescription();
            }
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
        try {
            this.delegate = new BaseCukeSpace(this.getTestClass().getJavaClass());
        } catch (final InitializationError e1) {
            // no-op: will not accur...if so this exception is not your biggest issue
        }

        // run @Test methods
        for (final FrameworkMethod each : ArquillianCucumber.super.computeTestMethods()) {
            // methods.add(each);
        }
        // hello ed

        methods.addAll(getChildren());
        try { // run cucumber, this looks like a hack but that's to keep @Before/@After/...
              // hooks behavior
            final Method runCucumber = BaseCukeSpace.class.getDeclaredMethod(RUN_CUCUMBER_MTD);
            this.delegate.getChildren().forEach(child -> methods.add(
                    new InstanceControlledFrameworkMethod(this, this.delegate.getCukeTestClass(), runCucumber, child)));
            // methods.add(new InstanceControlledFrameworkMethod(this,
            // this.delegate.getCukeTestClass(), runCucumber));
        } catch (final NoSuchMethodException e) {
            System.out.println("It did occur, and we do have issues");
            // no-op: will not accur...if so this exception is not your biggest issue
        }
        return methods;
    }

    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        this.delegate.run(notifier);
    }

    public static class InstanceControlledFrameworkMethod extends FrameworkMethod {
        private final ArquillianCucumber instance;
        private final Class<?>           originalClass;
        private ParentRunner<?>          child;

        private InstanceControlledFrameworkMethod(final ArquillianCucumber runner, final Class<?> originalClass,
                final Method runCucumber) {
            super(runCucumber);
            this.originalClass = originalClass;
            this.instance = runner;
        }

        private InstanceControlledFrameworkMethod(final ArquillianCucumber runner, final Class<?> originalClass,
                final Method runCucumber, final ParentRunner<?> child) {
            super(runCucumber);
            this.originalClass = originalClass;
            this.instance = runner;
            this.child = child;
        }

        @Override
        public Object invokeExplosively(final Object target, final Object... params) throws Throwable {
            return null;
        }

        public Class<?> getOriginalClass() {
            return originalClass;
        }

        public ParentRunner<?> getRunner() {
            return this.child;
        }

        /**
         * @return the instance
         */
        public ArquillianCucumber getInstance() {
            return instance;
        }
    }
}