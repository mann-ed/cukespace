package com.github.cukespace.examples;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import cucumber.runtime.arquillian.domain.Belly;
import cucumber.runtime.arquillian.feature.en.glue.CukesInBellyTest;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.CukeSpace;

/**
 * @author Edward Mann <ed.mann@edmann.com>
 *         <p>
 *         Created: Mar 17, 2021
 */
@RunWith(CukeSpace.class)
//@Features("src/test/resources/cucumber/runtime/arquillian/feature") // folder on the file system
/*
 * @Tags("@myTag")
 *
 */
@CucumberOptions(features = { "src/test/resources/cucumber/runtime/arquillian/feature" }, glue = {
        "cucumber.runtime.arquillian.feature.en.glue" }, tags = "@myTag")
public class RunCompleteCukeGlueTest {

    @Deployment
    public static WebArchive war() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Greeter.class, Belly.class, CukesInBellyTest.class);
        System.out.println(war.toString(true));
        return war;
    }

    @Inject
    Greeter greeter;

    @Test
    public void testNothing() {
        Assert.assertEquals("Hello, Earthling!", greeter.createGreeting("Earthling"));
        greeter.greet(System.out, "Earthling");
    }
}
