package org.quality.gates.sonar.api;

import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.quality.gates.jenkins.plugin.SonarInstance;
import org.quality.gates.sonar.api80.SonarHttpRequester80;
import org.quality.gates.sonar.api88.SonarHttpRequester88;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
class SonarHttpRequesterFactory {

    private static final String SONAR_API_SERVER_VERSION = "/api/server/version";

    static SonarHttpRequester getSonarHttpRequester(SonarInstance sonarInstance) {
        var request = new HttpGet(getSonarApiServerVersion(sonarInstance));
        var context = HttpClientContext.create();

        try (var client = HttpClientBuilder.create().build();
                var response = client.execute(request, context, classicHttpResponse -> classicHttpResponse)) {
            var sonarVersion = EntityUtils.toString(response.getEntity());

            if (majorSonarVersion(sonarVersion) == 8 && minorSonarVersion(sonarVersion) <= 7) {
                return new SonarHttpRequester80();
            } else if (majorSonarVersion(sonarVersion) >= 8) {
                return new SonarHttpRequester88();
            } else {
                throw new UnsuportedVersionException("Plugin doesn't support this version of sonar api!");
            }
        } catch (IOException | ParseException e) {
            throw new ApiConnectionException(e.getLocalizedMessage(),e);
        }
    }

    private static String getSonarApiServerVersion(SonarInstance sonarInstance) {
        return sonarInstance.getUrl() + SONAR_API_SERVER_VERSION;
    }

    private static int majorSonarVersion(String sonarVersion) {
        return Integer.parseInt(sonarVersion.split("\\.")[0]);
    }

    private static int minorSonarVersion(String sonarVersion) {
        return Integer.parseInt(sonarVersion.split("\\.")[1]);
    }
}
