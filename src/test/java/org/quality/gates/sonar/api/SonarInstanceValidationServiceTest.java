package org.quality.gates.sonar.api;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.quality.gates.jenkins.plugin.SonarInstance;

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
        SonarInstance globalConfigDataForSonarInstance = new SonarInstance("", "", "", "");
        assertEquals(
                "http://localhost:9000", sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUrlNormalUrlShouldReturnGivenUrl() {
        SonarInstance globalConfigDataForSonarInstance = new SonarInstance("", HTTP_MY_URL_COM_9000, "", "");
        assertEquals(
                HTTP_MY_URL_COM_9000, sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUrlNormalUrlWithSlashInTheEndShouldStripTheSlash() {
        SonarInstance globalConfigDataForSonarInstance = new SonarInstance("", "http://myUrl.com:9000/", "", "");
        assertEquals(
                HTTP_MY_URL_COM_9000, sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUsernameEmptyUsernameShouldReturnDefaultUsername() {
        SonarInstance globalConfigDataForSonarInstance = new SonarInstance("", "", "", "");
        assertEquals(ADMIN, sonarInstanceValidationService.validateUsername(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUsernameGivenUsernameShouldReturnGivenUsername() {
        SonarInstance globalConfigDataForSonarInstance = new SonarInstance("", "", "myUser", "");
        assertEquals(MY_USER, sonarInstanceValidationService.validateUsername(globalConfigDataForSonarInstance));
    }
}
