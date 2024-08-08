package org.quality.gates.sonar.api;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import java.util.Optional;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;

public class SonarInstanceValidationService {

    String validateUrl(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        var sonarUrl = Optional.of(globalConfigDataForSonarInstance.getSonarUrl())
                .orElse(GlobalConfigDataForSonarInstance.DEFAULT_URL);

        if (sonarUrl.endsWith("/")) {
            sonarUrl = sonarUrl.substring(0, sonarUrl.length() - 1);
        }

        return sonarUrl;
    }

    String validateCredentialsId(GlobalConfigDataForSonarInstance configData) {
        var credentialsId = configData.getSonarCredentialsId();
        var credentials = CredentialsProvider.listCredentialsInItem(
                StandardUsernamePasswordCredentials.class, null, null, null, null);

        credentials.stream().filter(c -> credentialsId.equals(c.name)).findFirst();

        return credentialsId;
    }

    GlobalConfigDataForSonarInstance validateData(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        return new GlobalConfigDataForSonarInstance(
                globalConfigDataForSonarInstance.getName(),
                validateUrl(globalConfigDataForSonarInstance),
                validateCredentialsId(globalConfigDataForSonarInstance),
                globalConfigDataForSonarInstance.getTimeToWait(),
                globalConfigDataForSonarInstance.getMaxWaitTime());
    }
}
