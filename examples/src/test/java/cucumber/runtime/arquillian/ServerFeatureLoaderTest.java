package cucumber.runtime.arquillian;

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.junit.ArquillianCucumber;

@RunWith(ArquillianCucumber.class)
//@Features(value = "jira/PROJECT-001.feature", loaders = ServerFeatureLoaderTest.MyLoader.class)
public class ServerFeatureLoaderTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addClass(Person.class);
    }

    @Inject
    private Person person;

    private String said;

    @When("^I say \"(.*?)\"$")
    public void speak(final String word) {
        said = person.say(word);
    }

    @Given("^I have a person$")
    public void init() {
        assertNotNull(person);
    }

    @Then("^I should have said \"(.*?)\"$")
    public void check(final String word) {
        assertEquals(word, said);
    }

    public static class Person {
        public String say(final String word) {
            System.out.println("Saying > " + word);
            return word;
        }
    }

    /*
     * public static class MyLoader implements ResourceLoader {
     *
     * @Override public Iterable<Resource> resources(final String path, final String
     * suffix) { if (!path.equals("jira/PROJECT-001") && !suffix.equals(".feature"))
     * { return emptyList(); } final Resource resource = new Resource() {
     *
     * @Override public String getPath() { return "jira/PROJECT-001.feature"; }
     *
     * @Override public String getAbsolutePath() { return null; }
     *
     * @Override public InputStream getInputStream() throws IOException { return new
     * ByteArrayInputStream( ("Feature: Say hello\n" + "\n" +
     * "  Scenario: Be polite\n" + "\n" + "    Given I have a person\n" +
     * "    When I say \"hi\"\n" +
     * "    Then I should have said \"hi\"\n").getBytes()); }
     *
     * @Override public String getClassName(final String extension) { throw new
     * UnsupportedOperationException(); } }; return asList(resource); } }
     */
}
