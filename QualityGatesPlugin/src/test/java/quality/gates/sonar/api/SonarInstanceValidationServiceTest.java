package quality.gates.sonar.api;

import org.junit.Before;
import org.junit.Test;
import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;

import static org.junit.Assert.*;

public class SonarInstanceValidationServiceTest {

    public static final String HTTP_MY_URL_COM_9000 = "http://myUrl.com:9000";
    public static final String ADMIN = "admin";
    public static final String MY_PASS = "myPass";
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
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("",  HTTP_MY_URL_COM_9000, "", "");
        assertEquals(HTTP_MY_URL_COM_9000, sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUrlNormalUrlWithSlashInTheEndShouldStripTheSlash(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "http://myUrl.com:9000/", "", "");
        assertEquals(HTTP_MY_URL_COM_9000, sonarInstanceValidationService.validateUrl(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUsernameEmptyUsernameShouldReturnDefaultUsername(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "", "");
        assertEquals(ADMIN, sonarInstanceValidationService.validateUsername(globalConfigDataForSonarInstance));
    }

    @Test
    public void testValidateUsernameGivenUsernameShouldReturnGivenUsername(){
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "myUser", "");
        assertEquals("myUser", sonarInstanceValidationService.validateUsername(globalConfigDataForSonarInstance));
    }

//    @Test
//    public void testValidatePasswordEmptyUsernameShouldReturnDefaultPassword(){
//        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "", "");
//        assertEquals(ADMIN, sonarInstanceValidationService.validatePassword(globalConfigDataForSonarInstance));
//    }
//
//    @Test
//    public void testValidatePasswordGivenUsernameShouldReturnGivenPassword(){
//        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "", "", MY_PASS);
//        assertEquals(MY_PASS, sonarInstanceValidationService.validatePassword(globalConfigDataForSonarInstance));
//    }
//
//    @Test
//    public void testValidateData(){
//        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "http://google.com", "", "");
//        assertEquals(new GlobalConfigDataForSonarInstance("", "http://google.com", ADMIN, ADMIN), sonarInstanceValidationService.validateData(globalConfigDataForSonarInstance));
//    }

}