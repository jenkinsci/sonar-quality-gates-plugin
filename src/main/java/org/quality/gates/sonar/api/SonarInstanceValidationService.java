package org.quality.gates.sonar.api;

import hudson.util.Secret;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;

public class SonarInstanceValidationService {

    String validateUrl(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        String sonarUrl;

        if (globalConfigDataForSonarInstance.getSonarUrl().isEmpty()) {
            sonarUrl = GlobalConfigDataForSonarInstance.DEFAULT_URL;
        } else {
            sonarUrl = globalConfigDataForSonarInstance.getSonarUrl();
            if (sonarUrl.endsWith("/")) {
                sonarUrl = sonarUrl.substring(0, sonarUrl.length() - 1);
            }
        }

        return sonarUrl;
    }

    String validateUsername(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        String sonarUsername;

        if (globalConfigDataForSonarInstance.getUsername().isEmpty()) {
            sonarUsername = GlobalConfigDataForSonarInstance.DEFAULT_USERNAME;
        } else {
            sonarUsername = globalConfigDataForSonarInstance.getUsername();
        }

        return sonarUsername;
    }

    Secret validatePassword(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        String sonarPassword;

        if (globalConfigDataForSonarInstance.getPass().isEmpty()) {
            sonarPassword = GlobalConfigDataForSonarInstance.DEFAULT_PASS;
        } else {
            sonarPassword = globalConfigDataForSonarInstance.getPass();
        }

        return Secret.fromString(sonarPassword);
    }
    
    String validateToken(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        String sonarToken;

        if (globalConfigDataForSonarInstance.getToken().isEmpty()) {
        	sonarToken = GlobalConfigDataForSonarInstance.DEFAULT_TOKEN;
        } else {
        	sonarToken = globalConfigDataForSonarInstance.getToken();
        }

        return sonarToken;
    }

    GlobalConfigDataForSonarInstance validateData(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        return new GlobalConfigDataForSonarInstance(
                globalConfigDataForSonarInstance.getName(),
                validateUrl(globalConfigDataForSonarInstance),
                validateToken(globalConfigDataForSonarInstance));
    }
}
