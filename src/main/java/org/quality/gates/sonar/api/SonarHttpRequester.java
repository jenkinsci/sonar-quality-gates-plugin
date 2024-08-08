package org.quality.gates.sonar.api;

import static com.cloudbees.plugins.credentials.CredentialsProvider.findCredentialById;
import static org.apache.commons.lang.StringUtils.isEmpty;

import hudson.model.Run;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.jenkins.plugin.JobConfigData;
import org.quality.gates.jenkins.plugin.QGException;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
public abstract class SonarHttpRequester {

    private String token;

    protected abstract String getSonarApiComponentShow();

    private void loginApi(GlobalConfigDataForSonarInstance configData, Run<?, ?> run) {
        var credentialsId = configData.getSonarCredentialsId();

        if (isEmpty(credentialsId)) {
            throw new CredentialsNotConfiguredException("sonarCredentialId is empty.");
        }

        var cred = findCredentialById(credentialsId, StringCredentialsImpl.class, run);

        if (cred == null) {
            throw new RuntimeException("credentials not found: " + configData.getSonarCredentialsId());
        }

        token = cred.getSecret().getPlainText();
    }

    protected abstract String getSonarApiLogin();

    private String executeGetRequest(CloseableHttpClient client, HttpGet request) throws QGException {
        var authHeader = "Bearer " + token;
        request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);

        try (var response = client.execute(request)) {
            var statusCode = response.getStatusLine().getStatusCode();
            var entity = response.getEntity();
            var returnResponse = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            if (statusCode != 200) {
                throw new QGException("Expected status 200, got: " + statusCode + ". Response: " + returnResponse);
            }

            return returnResponse;
        } catch (IOException e) {
            throw new QGException("GET execution error", e);
        }
    }

    String getAPITaskInfo(
            JobConfigData configData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance, Run<?, ?> run)
            throws QGException {
        loginApi(globalConfigDataForSonarInstance, run);

        var sonarProjectKey = getSonarApiTaskInfoParameter(configData, globalConfigDataForSonarInstance);
        var sonarProjectTaskInfoPath = getSonarApiTaskInfoUrl();
        var sonarHostUrl = globalConfigDataForSonarInstance.getSonarUrl();
        var taskInfoUri = sonarHostUrl
                + String.format(sonarProjectTaskInfoPath, URLEncoder.encode(sonarProjectKey, StandardCharsets.UTF_8));

        var httpClient = HttpClientBuilder.create().build();
        var request = new HttpGet(taskInfoUri);

        return executeGetRequest(httpClient, request);
    }

    protected abstract String getSonarApiTaskInfoUrl();

    protected abstract String getSonarApiTaskInfoParameter(
            JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance);

    String getAPIInfo(
            JobConfigData configData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance, Run<?, ?> run)
            throws QGException {
        loginApi(globalConfigDataForSonarInstance, run);

        var sonarApiQualityGates = globalConfigDataForSonarInstance.getSonarUrl()
                + String.format(getSonarApiQualityGatesStatusUrl(), configData.getProjectKey());
        var request = new HttpGet(String.format(sonarApiQualityGates, configData.getProjectKey()));
        var httpClient = HttpClientBuilder.create().build();

        return executeGetRequest(httpClient, request);
    }

    protected abstract String getSonarApiQualityGatesStatusUrl();
}
