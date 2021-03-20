package io.cucumber.arquillian.junit;

import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.junit.State;
import org.jboss.arquillian.test.spi.TestResult;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 18, 2021
 */
public class CukespaceTestRunner implements TestRunner {
    /**
     * Overwrite to provide additional run listeners.
     */
    protected List<RunListener> getRunListeners() {
        return Collections.emptyList();
    }

    @Override
    public TestResult execute(final Class<?> testClass, final String methodName) {
        TestResult                    testResult;
        final ExpectedExceptionHolder exceptionHolder = new ExpectedExceptionHolder();
        try {
            final JUnitCore runner = new JUnitCore();

            runner.addListener(exceptionHolder);

            for (final RunListener listener : getRunListeners())
                runner.addListener(listener);

            final Result result = runner.run(Request.method(testClass, methodName));

            if (result.getFailureCount() > 0) {
                testResult = TestResult.failed(exceptionHolder.getException());
            } else if (result.getIgnoreCount() > 0) {
                testResult = TestResult.skipped(); // Will this ever happen incontainer?
            } else {
                testResult = TestResult.passed();
            }
            if (testResult.getThrowable() == null) {
                testResult.setThrowable(exceptionHolder.getException());
            }
        } catch (final Throwable th) {
            testResult = TestResult.failed(th);
        }
        if (testResult.getThrowable() instanceof AssumptionViolatedException) {
            testResult = TestResult.skipped(testResult.getThrowable());
        }
        testResult.setEnd(System.currentTimeMillis());
        return testResult;
    }

    private class ExpectedExceptionHolder extends RunListener {
        private Throwable exception;

        public Throwable getException() {
            return exception;
        }

        @Override
        public void testAssumptionFailure(final Failure failure) {
            // AssumptionViolatedException might not be Serializable. Recreate with only
            // String message.
            exception = new AssumptionViolatedException(failure.getException().getMessage());
            exception.setStackTrace(failure.getException().getStackTrace());
            ;
        }

        @Override
        public void testFailure(final Failure failure) throws Exception {
            if (exception != null) {
                // In case of multiple errors only keep the first exception
                return;
            }
            exception = State.getTestException();
            final Test test = failure.getDescription().getAnnotation(Test.class);
            if (!(test != null && test.expected() != Test.None.class)) {
                // Not Expected Exception, and non thrown internally
                if (exception == null) {
                    exception = failure.getException();
                }
            }
        }

        @Override
        public void testFinished(final Description description) throws Exception {
            final Test test = description.getAnnotation(Test.class);
            if (test != null && test.expected() != Test.None.class) {
                if (exception == null) {
                    exception = State.getTestException();
                }
            }
            State.caughtTestException(null);
        }
    }
}