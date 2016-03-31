package quality.gates.sonar.api;

import org.junit.Before;
import org.junit.Test;
import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;

import static org.junit.Assert.*;

public class SonarInstanceValidationServiceTest {

    SonarInstanceValidationService sonarInstanceValidationService;

    @Before
    public void setUp() {
        sonarInstanceValidationService = new SonarInstanceValidationService();
    }

    @Test
    public void testValidateUrlEmptySonarUrlShouldReturnDefaultUrl(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "", "");
        assertEquals("http://localhost:9000", sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUrlNormalUrlShouldReturnGivenUrl(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "http://myUrl.com:9000", "", "");
        assertEquals("http://myUrl.com:9000", sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUrlNormalUrlWithSlashInTheEndShouldStripTheSlash(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "http://myUrl.com:9000/", "", "");
        assertEquals("http://myUrl.com:9000", sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUsernameEmptyUsernameShouldReturnDefaultUsername(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "", "");
        assertEquals("admin", sonarInstanceValidationService.validateUsername(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUsernameGivenUsernameShouldReturnGivenUsername(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "myUser", "");
        assertEquals("myUser", sonarInstanceValidationService.validateUsername(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidatePasswordEmptyUsernameShouldReturnDefaultPassword(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "", "");
        assertEquals("admin", sonarInstanceValidationService.validatePassword(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidatePasswordGivenUsernameShouldReturnGivenPassword(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "", "myPass");
        assertEquals("myPass", sonarInstanceValidationService.validatePassword(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateData(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "http://google.com", "", "");
        assertEquals(new GlobalConfigDataForSonarInstance("", "http://google.com", "admin", "admin"), sonarInstanceValidationService.validateData(globalConfigDataForSonarInstance));
    }

}