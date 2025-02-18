package org.quality.gates.sonar.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quality.gates.jenkins.plugin.SonarInstance;

class SonarInstanceValidationServiceTest {

    private static final String HTTP_MY_URL_COM_9000 = "http://myUrl.com:9000";

    private static final String ADMIN = "admin";

    private static final String MY_USER = "myUser";

    private SonarInstanceValidationService sonarInstanceValidationService;

    @BeforeEach
    void setUp() {
        sonarInstanceValidationService = new SonarInstanceValidationService();
    }

    @Test
    void testValidateUrlEmptySonarUrlShouldReturnDefaultUrl() {
        SonarInstance sonarInstance = new SonarInstance("", "", "", "");
        assertEquals("http://localhost:9000", sonarInstanceValidationService.validateUrl(sonarInstance));
    }

    @Test
    void testValidateUrlNormalUrlShouldReturnGivenUrl() {
        SonarInstance sonarInstance = new SonarInstance("", HTTP_MY_URL_COM_9000, "", "");
        assertEquals(HTTP_MY_URL_COM_9000, sonarInstanceValidationService.validateUrl(sonarInstance));
    }

    @Test
    void testValidateUrlNormalUrlWithSlashInTheEndShouldStripTheSlash() {
        SonarInstance sonarInstance = new SonarInstance("", "http://myUrl.com:9000/", "", "");
        assertEquals(HTTP_MY_URL_COM_9000, sonarInstanceValidationService.validateUrl(sonarInstance));
    }

    @Test
    void testValidateUsernameEmptyUsernameShouldReturnDefaultUsername() {
        SonarInstance sonarInstance = new SonarInstance("", "", "", "");
        assertEquals(ADMIN, sonarInstanceValidationService.validateUsername(sonarInstance));
    }

    @Test
    void testValidateUsernameGivenUsernameShouldReturnGivenUsername() {
        SonarInstance sonarInstance = new SonarInstance("", "", "myUser", "");
        assertEquals(MY_USER, sonarInstanceValidationService.validateUsername(sonarInstance));
    }
}
