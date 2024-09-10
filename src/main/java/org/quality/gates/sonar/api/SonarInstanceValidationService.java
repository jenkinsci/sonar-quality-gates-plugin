package org.quality.gates.sonar.api;

import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
import org.quality.gates.jenkins.plugin.SonarInstance;

public class SonarInstanceValidationService {

    String validateUrl(SonarInstance globalConfigDataForSonarInstance) {
        if (globalConfigDataForSonarInstance.getSonarUrl().isEmpty()) {
            return SonarInstance.DEFAULT_URL;
        }

        var sonarUrl = globalConfigDataForSonarInstance.getSonarUrl();
        if (sonarUrl.endsWith("/")) {
            sonarUrl = sonarUrl.substring(0, sonarUrl.length() - 1);
        }

        return sonarUrl;
    }

    String validateUsername(SonarInstance globalConfigDataForSonarInstance) {
        if (globalConfigDataForSonarInstance.getUsername().isEmpty()) {
            return SonarInstance.DEFAULT_USERNAME;
        }

        return globalConfigDataForSonarInstance.getUsername();
    }

    private Secret validatePassword(SonarInstance globalConfigDataForSonarInstance) {
        if (globalConfigDataForSonarInstance.getPass().getPlainText().isEmpty()) {
            return Secret.fromString(SonarInstance.DEFAULT_PASS);
        }

        return globalConfigDataForSonarInstance.getPass();
    }

    SonarInstance validateData(SonarInstance globalConfigDataForSonarInstance) {
        if (StringUtils.isNotEmpty(globalConfigDataForSonarInstance.getToken().getPlainText())) {
            return new SonarInstance(
                    globalConfigDataForSonarInstance.getName(),
                    validateUrl(globalConfigDataForSonarInstance),
                    globalConfigDataForSonarInstance.getToken(),
                    globalConfigDataForSonarInstance.getTimeToWait(),
                    globalConfigDataForSonarInstance.getMaxWaitTime());
        }

        return new SonarInstance(
                globalConfigDataForSonarInstance.getName(),
                validateUrl(globalConfigDataForSonarInstance),
                validateUsername(globalConfigDataForSonarInstance),
                validatePassword(globalConfigDataForSonarInstance),
                globalConfigDataForSonarInstance.getTimeToWait(),
                globalConfigDataForSonarInstance.getMaxWaitTime());
    }
}
