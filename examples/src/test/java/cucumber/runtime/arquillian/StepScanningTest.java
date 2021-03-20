package cucumber.runtime.arquillian;

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import cucumber.runtime.arquillian.domain.Belly;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.CukeSpace;

@RunWith(CukeSpace.class)
@CucumberOptions(glue = "cucumber.runtime.arquillian.step", features = "src/test/resources/features")
public class StepScanningTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return create(WebArchive.class).addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addClasses(Belly.class);
    }
}
