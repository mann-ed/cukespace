package cucumber.runtime.arquillian.feature.en.glue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import com.github.cukespace.examples.Greeter;

import cucumber.runtime.arquillian.domain.Belly;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.junit.ArquillianCucumber;

//@Features("cucumber/runtime/arquillian/feature/cukes-in-belly.feature:9") // only second scenario
@RunWith(ArquillianCucumber.class)
public class CukesInBellyTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addClass(Belly.class);
    }

    @Inject
    private Belly   belly;

    @Inject
    private Greeter greeter;

    @When("^I eat (\\d+) cukes$")
    public void eatCukes(final int cukes) {
        belly.setCukes(cukes);
    }

    @Given("^I have a belly$")
    public void setUpBelly() {
        assertThat(greeter, is(not(nullValue())));
        belly = new Belly();
    }

    @Then("^I should have (\\d+) cukes in my belly$")
    public void shouldHaveThisMany(final int cukes) {
        assertEquals(cukes, belly.getCukes());
    }
}
