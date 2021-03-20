package io.cucumber.junit;

import static io.cucumber.junit.FileNameCompatibleNames.createName;
import static io.cucumber.junit.FileNameCompatibleNames.uniqueSuffix;
import static io.cucumber.junit.PickleRunners.withStepDescriptions;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.junit.PickleRunners.PickleId;
import io.cucumber.junit.PickleRunners.PickleRunner;
import io.cucumber.plugin.event.Step;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 18, 2021
 */
public class CukeRunners extends ParentRunner<PickleRunner> {

    private final List<PickleRunner> children;
    private final Feature            feature;
    private final JUnitOptions       options;
    private final Integer            uniqueSuffix;
    private Description              description;

    private CukeRunners(final Feature feature, final Integer uniqueSuffix, final Predicate<Pickle> filter,
            final RunnerSupplier runners, final JUnitOptions options) throws InitializationError {
        super((Class<?>) null);
        this.feature = feature;
        this.uniqueSuffix = uniqueSuffix;
        this.options = options;

        final Map<String, List<Pickle>> groupedByName = feature.getPickles().stream()
                .collect(groupingBy(Pickle::getName));
        this.children = feature.getPickles().stream().filter(filter).map(pickle -> {
            final String  featureName = getName();
            final Integer exampleId   = uniqueSuffix(groupedByName, pickle, Pickle::getName);
            return options.stepNotifications() ? withStepDescriptions(runners, pickle, exampleId, options)
                    : withNoCukeSpaceStepDescriptions(featureName, runners, pickle, exampleId, options);
        }).collect(toList());
    }

    static CukeRunners create(final Feature feature, final Integer uniqueSuffix, final Predicate<Pickle> filter,
            final RunnerSupplier runners, final JUnitOptions options) {
        try {
            return new CukeRunners(feature, uniqueSuffix, filter, runners, options);
        } catch (final InitializationError e) {
            throw new CucumberException("Failed to create scenario runner", e);
        }
    }

    boolean isEmpty() {
        return children.isEmpty();
    }

    private static final class FeatureId implements Serializable {

        private static final long serialVersionUID = 1L;
        private final URI         uri;

        FeatureId(final Feature feature) {
            this.uri = feature.getUri();
        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            final FeatureId featureId = (FeatureId) o;
            return uri.equals(featureId.uri);
        }

        @Override
        public String toString() {
            return uri.toString();
        }

    }

    @Override
    protected String getName() {
        final String name = feature.getName().orElse("EMPTY_NAME");
        return createName(name, uniqueSuffix, options.filenameCompatibleNames());
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), new FeatureId(feature));
            getChildren().forEach(child -> description.addChild(describeChild(child)));
        }
        return description;
    }

    @Override
    protected List<PickleRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(final PickleRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(final PickleRunner child, final RunNotifier notifier) {
        notifier.fireTestStarted(describeChild(child));
        try {
            child.run(notifier);
        } catch (final Throwable e) {
            notifier.fireTestFailure(new Failure(describeChild(child), e));
            notifier.pleaseStop();
        } finally {
            notifier.fireTestFinished(describeChild(child));
        }
    }

    static PickleRunner withNoCukeSpaceStepDescriptions(final String featureName, final RunnerSupplier runnerSupplier,
            final Pickle pickle, final Integer uniqueSuffix, final JUnitOptions jUnitOptions) {
        return new NoCukeSpaceStepDescriptions(featureName, runnerSupplier, pickle, uniqueSuffix, jUnitOptions);
    }

    static final class NoCukeSpaceStepDescriptions implements PickleRunner {

        private final String         featureName;
        private final RunnerSupplier runnerSupplier;
        private final Pickle         pickle;
        private final JUnitOptions   jUnitOptions;
        private final Integer        uniqueSuffix;
        private Description          description;

        NoCukeSpaceStepDescriptions(final String featureName, final RunnerSupplier runnerSupplier, final Pickle pickle,
                final Integer uniqueSuffix, final JUnitOptions jUnitOptions) {
            this.featureName = featureName;
            this.runnerSupplier = runnerSupplier;
            this.pickle = pickle;
            this.jUnitOptions = jUnitOptions;
            this.uniqueSuffix = uniqueSuffix;
        }

        @Override
        public void run(final RunNotifier notifier) {
            // Possibly invoked by a thread other then the creating thread
            final Runner        runner        = runnerSupplier.get();
            final JUnitReporter jUnitReporter = new JUnitReporter(runner.getBus(), jUnitOptions);
            jUnitReporter.startExecutionUnit(this, notifier);
            runner.runPickle(pickle);
            jUnitReporter.finishExecutionUnit();
        }

        @Override
        public Description getDescription() {
            if (description == null) {
                final String className = createName(featureName, jUnitOptions.filenameCompatibleNames());
                final String name      = createName(pickle.getName(), uniqueSuffix,
                        jUnitOptions.filenameCompatibleNames());
                description = Description.createTestDescription(className, name, new PickleId(pickle));
            }
            return description;
        }

        @Override
        public Description describeChild(final Step step) {
            throw new UnsupportedOperationException("This pickle runner does not wish to describe its children");
        }

    }

}
