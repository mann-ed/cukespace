package cucumber.runtime.arquillian.feature;

import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import io.cucumber.arquillian.junit.ArquillianCucumber;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

@RunWith(ArquillianCucumber.class)
public class ScenarioOutlineTest {
    @Deployment
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @And("^que he introducido \"([^\"]*)\"$")
    public void que_he_introducido(final String arg1) throws Throwable {
        assertNotNull(arg1);
    }

    @When("^oprimo el  \"([^\"]*)\"$")
    public void oprimo_el(final String arg1) throws Throwable {
        assertNotNull(arg1);
    }

    @Then("^el nombre del  \"([^\"]*)\" se muestra en la pantalla$")
    public void el_nombre_del_se_muestra_en_la_pantalla(final String arg1) throws Throwable {
        assertNotNull(arg1);
    }

    @Given("^visito  la pagina \"([^\"]*)\"$")
    public void visito_la_pagina(final String arg1) throws Throwable {
        assertNotNull(arg1);
    }

    @And("^doy click en el link  \"([^\"]*)\"$")
    public void doy_click_en_el_link(final String arg1) throws Throwable {
        assertNotNull(arg1);
    }
}
