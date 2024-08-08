package org.quality.gates.sonar.api;

import java.io.IOException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.sonar.api80.SonarHttpRequester80;
import org.quality.gates.sonar.api88.SonarHttpRequester88;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
class SonarHttpRequesterFactory {

    private static final String SONAR_API_SERVER_VERSION = "/api/server/version";

    static SonarHttpRequester getSonarHttpRequester(GlobalConfigDataForSonarInstance configData) {
        var request = new HttpGet(getSonarApiServerVersion(configData));
        var context = HttpClientContext.create();

        try (var client = HttpClientBuilder.create().build();
                var response = client.execute(request, context)) {
            var sonarVersion = EntityUtils.toString(response.getEntity());

            if (majorSonarVersion(sonarVersion) == 8 && minorSonarVersion(sonarVersion) <= 7) {
                return new SonarHttpRequester80();
            } else if (majorSonarVersion(sonarVersion) >= 8) {
                return new SonarHttpRequester88();
            } else {
                throw new UnsupportedVersionException(
                        "Plugin doesn't suport this version of sonar api! Please contact the developer.");
            }
        } catch (IOException e) {
            throw new ApiConnectionException(e.getLocalizedMessage());
        }
    }

    private static String getSonarApiServerVersion(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        return globalConfigDataForSonarInstance.getSonarUrl() + SONAR_API_SERVER_VERSION;
    }

    private static int majorSonarVersion(String sonarVersion) {
        return Integer.parseInt(sonarVersion.split("\\.")[0]);
    }

    private static int minorSonarVersion(String sonarVersion) {
        return Integer.parseInt(sonarVersion.split("\\.")[1]);
    }
}
