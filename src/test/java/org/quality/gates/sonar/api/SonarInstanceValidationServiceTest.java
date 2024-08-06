package org.quality.gates.sonar.api;

import org.junit.Before;
import org.junit.Test;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.sonar.api.SonarInstanceValidationService;

import static org.junit.Assert.assertEquals;

public class SonarInstanceValidationServiceTest {

    private static final String HTTP_MY_URL_COM_9000 = "http://myUrl.com:9000";

    private static final String ADMIN = "admin";

    private static final String MY_USER = "myUser";

    private SonarInstanceValidationService sonarInstanceValidationService;

    @Before
    public void setUp() {
        sonarInstanceValidationService = new SonarInstanceValidationService();
    }

    @Test
    public void testValidateUrlEmptySonarUrlShouldReturnDefaultUrl() {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "", "");
        assertEquals("http://localhost:9000", sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUrlNormalUrlShouldReturnGivenUrl() {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", HTTP_MY_URL_COM_9000, "", "");
        assertEquals(HTTP_MY_URL_COM_9000, sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUrlNormalUrlWithSlashInTheEndShouldStripTheSlash() {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "http://myUrl.com:9000/", "", "");
        assertEquals(HTTP_MY_URL_COM_9000, sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUsernameEmptyUsernameShouldReturnDefaultUsername() {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "", "");
        assertEquals(ADMIN, sonarInstanceValidationService.validateUsername(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUsernameGivenUsernameShouldReturnGivenUsername() {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "myUser", "");
        assertEquals(MY_USER, sonarInstanceValidationService.validateUsername(globalConfigDataForSonarInstance));
    }
}
