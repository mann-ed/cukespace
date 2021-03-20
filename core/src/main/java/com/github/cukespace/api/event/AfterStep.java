package com.github.cukespace.api.event;

import io.cucumber.plugin.event.Step;

public class AfterStep extends StepEvent {
    public AfterStep(final String featurePath, final Step step) {
        super(featurePath, step);
    }
}
