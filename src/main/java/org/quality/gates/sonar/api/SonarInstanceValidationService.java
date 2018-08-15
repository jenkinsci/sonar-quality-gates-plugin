package org.quality.gates.sonar.api;

import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
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

    private Secret validatePassword(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        String sonarPassword;

        if (globalConfigDataForSonarInstance.getPass().isEmpty()) {
            sonarPassword = GlobalConfigDataForSonarInstance.DEFAULT_PASS;
        } else {
            sonarPassword = globalConfigDataForSonarInstance.getPass();
        }

        return Secret.fromString(sonarPassword);
    }

    GlobalConfigDataForSonarInstance validateData(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        if (StringUtils.isNotEmpty(globalConfigDataForSonarInstance.getToken())) {
            return new GlobalConfigDataForSonarInstance(
                    globalConfigDataForSonarInstance.getName(),
                    validateUrl(globalConfigDataForSonarInstance),
                    globalConfigDataForSonarInstance.getToken(),
                    globalConfigDataForSonarInstance.getTimeToWait(),
					globalConfigDataForSonarInstance.getMaxWaitTime());
        } else {
            return new GlobalConfigDataForSonarInstance(
                    globalConfigDataForSonarInstance.getName(),
                    validateUrl(globalConfigDataForSonarInstance),
                    validateUsername(globalConfigDataForSonarInstance),
                    validatePassword(globalConfigDataForSonarInstance),
                    globalConfigDataForSonarInstance.getTimeToWait(),
					globalConfigDataForSonarInstance.getMaxWaitTime());
        }
    }
}
