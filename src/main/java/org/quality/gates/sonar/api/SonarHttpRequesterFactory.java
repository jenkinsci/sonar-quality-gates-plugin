package org.quality.gates.sonar.api;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.sonar.api5x.SonarHttpRequester5x;
import org.quality.gates.sonar.api60.SonarHttpRequester60;
import org.quality.gates.sonar.api61.SonarHttpRequester61;

import java.io.IOException;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
class SonarHttpRequesterFactory {

    private static final String SONAR_API_SERVER_VERSION = "/api/server/version";

    static SonarHttpRequester getSonarHttpRequester(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        try {
            HttpGet request = new HttpGet(getSonarApiServerVersion(globalConfigDataForSonarInstance));

            HttpClientContext context = HttpClientContext.create();
            CloseableHttpClient client = HttpClientBuilder.create().build();
            CloseableHttpResponse response = client.execute(request, context);
            String sonarVersion = EntityUtils.toString(response.getEntity());

            if (majorSonarVersion(sonarVersion) <= 5) {
                return new SonarHttpRequester5x();
            } else if (majorSonarVersion(sonarVersion) >= 6 && minorSonarVersion(sonarVersion) == 0) {
                return new SonarHttpRequester60();
            } else if (majorSonarVersion(sonarVersion) >= 6 && minorSonarVersion(sonarVersion) >= 1) {
                return new SonarHttpRequester61();
            } else {
                throw new UnsuportedVersionException("Plugin doesn't suport this version of sonar api! Please contact the developer.");
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
