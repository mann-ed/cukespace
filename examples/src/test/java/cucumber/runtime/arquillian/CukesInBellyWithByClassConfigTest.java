package cucumber.runtime.arquillian;

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import cucumber.runtime.arquillian.domain.Belly;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.CukeSpace;

@RunWith(CukeSpace.class)
//@Features("src/test/resources/cucumber/runtime/arquillian/feature") // folder on the file system
//@Tags("@myTag")
@CucumberOptions(strict = true)
public class CukesInBellyWithByClassConfigTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addClass(Belly.class);
    }

    @Inject
    private Belly belly;

    @When("^I eat (\\d+) cukes$")
    public void eatCukes(final int cukes) {
        belly.setCukes(cukes);
    }

    @Given("^I have a belly$")
    public void setUpBelly() {
        belly = new Belly();
    }

    @Then("^I should have (\\d+) cukes in my belly$")
    public void shouldHaveThisMany(final int cukes) {
        assertEquals(cukes, belly.getCukes());
    }
}
