package org.quality.gates.jenkins.plugin.casc;

import static org.hamcrest.MatcherAssert.assertThat;

import io.jenkins.plugins.casc.misc.RoundTripAbstractTest;
import java.util.List;
import jenkins.model.GlobalConfiguration;
import org.jvnet.hudson.test.RestartableJenkinsRule;
import org.quality.gates.jenkins.plugin.GlobalSonarQualityGatesConfiguration;
import org.quality.gates.jenkins.plugin.SonarInstance;

public class ConfigurationAsCodeTest extends RoundTripAbstractTest {

    @Override
    protected String configResource() {
        return "configuration-as-code.yml";
    }

    @Override
    protected String stringInLogExpected() {
        return "name = test-name";
    }

    @Override
    protected void assertConfiguredAsExpected(RestartableJenkinsRule j, String configContent) {
        GlobalSonarQualityGatesConfiguration globalSonarQualityGatesConfiguration =
                GlobalConfiguration.all().get(GlobalSonarQualityGatesConfiguration.class);

        // Assert that globalSonarQualityGatesConfiguration is not null
        assertThat(
                "GlobalSonarQualityGatesConfiguration should not be null",
                globalSonarQualityGatesConfiguration != null);

        List<SonarInstance> sonarInstances = globalSonarQualityGatesConfiguration.getSonarInstances();

        // Assert that sonarInstances is not null
        assertThat("SonarInstances should not be null", sonarInstances != null);

        // Assert that the first sonarInstance is not null
        assertThat("First SonarInstance should not be null", sonarInstances.get(0) != null);

        SonarInstance sonarInstance = sonarInstances.get(0);

        // Assert the individual fields of sonarInstance
        assertThat(
                "SonarInstance name should be 'test-name'",
                sonarInstance.getName().equals("test-name"));
        assertThat(
                "SonarInstance username should be 'test-username'",
                sonarInstance.getUsername().equals("test-username"));
        assertThat(
                "SonarInstance pass should be 'test-password'",
                sonarInstance.getPass().getPlainText().equals("test-password"));
        assertThat(
                "SonarInstance url should be 'http://localhost:9000'",
                sonarInstance.getUrl().equals("http://localhost:9000"));
        assertThat("SonarInstance timeToWait should be 10000", sonarInstance.getTimeToWait() == 10000);
        assertThat("SonarInstance maxWaitTime should be 50000", sonarInstance.getMaxWaitTime() == 50000);
    }
}
