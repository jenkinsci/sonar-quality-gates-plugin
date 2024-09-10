package org.quality.gates.sonar.api;

import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
import org.quality.gates.jenkins.plugin.SonarInstance;

public class SonarInstanceValidationService {

    String validateUrl(SonarInstance sonarInstance) {
        if (sonarInstance.getUrl().isEmpty()) {
            return SonarInstance.DEFAULT_URL;
        }

        var url = sonarInstance.getUrl();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    String validateUsername(SonarInstance sonarInstance) {
        if (sonarInstance.getUsername().isEmpty()) {
            return SonarInstance.DEFAULT_USERNAME;
        }

        return sonarInstance.getUsername();
    }

    private Secret validatePassword(SonarInstance sonarInstance) {
        if (sonarInstance.getPass().getPlainText().isEmpty()) {
            return Secret.fromString(SonarInstance.DEFAULT_PASS);
        }

        return sonarInstance.getPass();
    }

    SonarInstance validateData(SonarInstance sonarInstance) {
        if (StringUtils.isNotEmpty(sonarInstance.getToken().getPlainText())) {
            return new SonarInstance(
                    sonarInstance.getName(),
                    validateUrl(sonarInstance),
                    sonarInstance.getToken(),
                    sonarInstance.getTimeToWait(),
                    sonarInstance.getMaxWaitTime());
        }

        return new SonarInstance(
                sonarInstance.getName(),
                validateUrl(sonarInstance),
                validateUsername(sonarInstance),
                validatePassword(sonarInstance),
                sonarInstance.getTimeToWait(),
                sonarInstance.getMaxWaitTime());
    }
}
