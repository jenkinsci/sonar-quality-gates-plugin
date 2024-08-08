package org.quality.gates.sonar.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;

public class SonarInstanceValidationServiceTest {

    private static final String HTTP_MY_URL_COM_9000 = "http://myUrl.com:9000";

    private SonarInstanceValidationService service;

    @Before
    public void setUp() {
        service = new SonarInstanceValidationService();
    }

    @Test
    public void testValidateUrlEmptySonarUrlShouldReturnDefaultUrl() {
        var expectedUrl = "http://localhost:9000";

        var globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "", 0, 0);

        assertEquals(expectedUrl, service.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUrlNormalUrlShouldReturnGivenUrl() {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance =
                new GlobalConfigDataForSonarInstance("", HTTP_MY_URL_COM_9000, "", 0, 0);
        assertEquals(HTTP_MY_URL_COM_9000, service.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUrlNormalUrlWithSlashInTheEndShouldStripTheSlash() {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance =
                new GlobalConfigDataForSonarInstance("", "http://myUrl.com:9000/", "", 0, 0);
        assertEquals(HTTP_MY_URL_COM_9000, service.validateUrl(globalConfigDataForSonarInstance));
    }
}
