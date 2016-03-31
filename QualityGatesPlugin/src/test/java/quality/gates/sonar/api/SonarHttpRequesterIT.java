package quality.gates.sonar.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
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

    @Before
    public void init() {
        globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("name","http://localhost:9876", "admin", "admin");
        sonarHttpRequester = new SonarHttpRequester();
    }

    @Test
    public void testPerformGetAPIInfo() throws Exception {
        int status = 200;
        String result = getResponse(status);
        assertTrue(result.equals("OK"));
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