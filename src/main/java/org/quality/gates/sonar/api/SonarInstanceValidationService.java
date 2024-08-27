package org.quality.gates.sonar.api;

import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;

public class SonarInstanceValidationService {

    String validateUrl(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        if (globalConfigDataForSonarInstance.getSonarUrl().isEmpty()) {
            return GlobalConfigDataForSonarInstance.DEFAULT_URL;
        }

        var sonarUrl = globalConfigDataForSonarInstance.getSonarUrl();
        if (sonarUrl.endsWith("/")) {
            sonarUrl = sonarUrl.substring(0, sonarUrl.length() - 1);
        }

        return sonarUrl;
    }

    String validateUsername(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        if (globalConfigDataForSonarInstance.getUsername().isEmpty()) {
            return GlobalConfigDataForSonarInstance.DEFAULT_USERNAME;
        }

        return globalConfigDataForSonarInstance.getUsername();
    }

    private Secret validatePassword(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        if (globalConfigDataForSonarInstance.getPass().getPlainText().isEmpty()) {
            return Secret.fromString(GlobalConfigDataForSonarInstance.DEFAULT_PASS);
        }

        return globalConfigDataForSonarInstance.getPass();
    }

    GlobalConfigDataForSonarInstance validateData(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        if (StringUtils.isNotEmpty(globalConfigDataForSonarInstance.getToken().getPlainText())) {
            return new GlobalConfigDataForSonarInstance(
                    globalConfigDataForSonarInstance.getName(),
                    validateUrl(globalConfigDataForSonarInstance),
                    globalConfigDataForSonarInstance.getToken(),
                    globalConfigDataForSonarInstance.getTimeToWait(),
                    globalConfigDataForSonarInstance.getMaxWaitTime());
        }

        return new GlobalConfigDataForSonarInstance(
                globalConfigDataForSonarInstance.getName(),
                validateUrl(globalConfigDataForSonarInstance),
                validateUsername(globalConfigDataForSonarInstance),
                validatePassword(globalConfigDataForSonarInstance),
                globalConfigDataForSonarInstance.getTimeToWait(),
                globalConfigDataForSonarInstance.getMaxWaitTime());
    }
}
