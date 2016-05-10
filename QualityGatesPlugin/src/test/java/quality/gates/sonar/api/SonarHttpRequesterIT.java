package quality.gates.sonar.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import hudson.util.Secret;
import org.jvnet.hudson.test.JenkinsRule;
import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import quality.gates.jenkins.plugin.JobConfigData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertTrue;


public class SonarHttpRequesterIT {

    private SonarHttpRequester sonarHttpRequester;

    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9876);
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void init() {
        globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("name","http://localhost:9876", "admin", Secret.fromString("admin"));
        sonarHttpRequester = new SonarHttpRequester();
    }

    @Test
    public void testPerformGetAPIInfo() {
        int status = 200;
        String result = getResponse(status);
        assertTrue("OK".equals(result));
    }

    private String getResponse(int status) {
        String projectKey = "com.opensource:quality-gates";

        stubFor(get(urlPathEqualTo("/api/events"))
                .withQueryParam("resource", equalTo(projectKey))
                .withQueryParam("format", equalTo("json"))
                .withQueryParam("categories", equalTo("Alert"))
                .willReturn(aResponse()
                        .withStatus(status).withBody("OK")));

        JobConfigData jobConfigData = new JobConfigData();
        jobConfigData.setProjectKey(projectKey);
        return sonarHttpRequester.getAPIInfo(jobConfigData, globalConfigDataForSonarInstance);
    }
}