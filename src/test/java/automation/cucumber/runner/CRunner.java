package automation.cucumber.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.junit.runner.RunWith;
import org.testng.annotations.DataProvider;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/java/automation/cucumber/features/PetStore",
        glue = {"automation.cucumber.hook", "automation.cucumber.steps"},
        plugin = {
                "pretty",
                "html:target/cucumber",
                "json:target/cucumber.json",
                "junit:build/test-results/test/xml/ejecucion.xml"
        },
        tags = "@CrearPet"
)

public class CRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }

}

