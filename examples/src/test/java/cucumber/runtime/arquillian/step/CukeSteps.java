package cucumber.runtime.arquillian.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;

import cucumber.runtime.arquillian.domain.Belly;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CukeSteps {
    @Inject
    private Belly belly;

    @Test
    public void ignored() {
        // TODO: remove it. Just here to let tomee enrich this class without help, issue
        // fixed in coming 1.7.2
    }

    @When("^I eat (\\d+) cukes$")
    public void eatCukes(final int cukes) {
        if (belly == null) {
            belly = new Belly();
        }
        belly.setCukes(cukes);
    }

    @Given("^I have a belly$")
    public void setUpBelly() {
        assertNotNull(belly);
    }

    @Then("^I should have (\\d+) cukes in my belly$")
    public void shouldHaveThisMany(final int cukes) {
        assertEquals(cukes, belly.getCukes());
    }
}
