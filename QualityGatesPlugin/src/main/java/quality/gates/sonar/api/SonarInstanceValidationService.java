package quality.gates.sonar.api;

import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;

public class SonarInstanceValidationService {

    public String validateUrl(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        String sonarUrl;
        if(globalConfigDataForSonarInstance.getSonarUrl().isEmpty())
            sonarUrl = GlobalConfigDataForSonarInstance.DEFAULT_URL;
        else {
            sonarUrl = globalConfigDataForSonarInstance.getSonarUrl();
            if(sonarUrl.endsWith("/"))
                sonarUrl = sonarUrl.substring(0,sonarUrl.length()-1);
        }
        return sonarUrl;
    }

    public String validateUsername(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        String sonarUsername;
        if(globalConfigDataForSonarInstance.getUsername().isEmpty())
            sonarUsername = GlobalConfigDataForSonarInstance.DEFAULT_USERNAME;
        else
            sonarUsername = globalConfigDataForSonarInstance.getUsername();
        return sonarUsername;
    }

    public String validatePassword(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        String sonarPassword;
        if(globalConfigDataForSonarInstance.getPassword().isEmpty())
            sonarPassword = GlobalConfigDataForSonarInstance.DEFAULT_PASSWORD;
        else
            sonarPassword = globalConfigDataForSonarInstance.getPassword();
        return sonarPassword;
    }

    public GlobalConfigDataForSonarInstance validateData(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        GlobalConfigDataForSonarInstance newGlobalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance(
                globalConfigDataForSonarInstance.getName(),
                validateUrl(globalConfigDataForSonarInstance),
                validateUsername(globalConfigDataForSonarInstance),
                validatePassword(globalConfigDataForSonarInstance));
        return newGlobalConfigDataForSonarInstance;
    }
}
