package com.github.cukespace.api.event;

import io.cucumber.plugin.event.Step;

public class BeforeStep extends StepEvent {
    public BeforeStep(final String featurePath, final Step step) {
        super(featurePath, step);
    }
}
