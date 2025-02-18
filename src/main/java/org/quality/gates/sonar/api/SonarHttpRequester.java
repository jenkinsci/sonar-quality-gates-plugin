package org.quality.gates.sonar.api;

import static java.net.URLEncoder.encode;

import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.quality.gates.jenkins.plugin.JobConfigData;
import org.quality.gates.jenkins.plugin.QGException;
import org.quality.gates.jenkins.plugin.SonarInstance;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
public abstract class SonarHttpRequester {

    private static final String SONAR_API_COMPONENT_SHOW = "/api/components/show?key=%s";

    /**
     * Cached client context for lazy login.
     * @see #loginApi(SonarInstance)
     */
    private transient HttpClientContext httpClientContext;

    /**
     * Cached client for lazy login.
     * @see #loginApi(SonarInstance)
     */
    private transient CloseableHttpClient httpClient;

    private boolean logged = false;

    private String token;

    protected String getSonarApiComponentShow() {
        return SONAR_API_COMPONENT_SHOW;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    private void loginApi(SonarInstance sonarInstance) {
        httpClientContext = HttpClientContext.create();

        if (StringUtils.isNotEmpty(sonarInstance.getToken().getPlainText())) {
            token = sonarInstance.getToken().getPlainText();
            httpClient = HttpClientBuilder.create().build();
        } else {
            var credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(null, -1),
                    new UsernamePasswordCredentials(
                            sonarInstance.getUsername(),
                            sonarInstance.getPass().getPlainText().toCharArray()));

            httpClient = HttpClientBuilder.create()
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .build();

            httpClientContext.setCredentialsProvider(credentialsProvider);

            var loginHttpPost = new HttpPost(sonarInstance.getUrl() + getSonarApiLogin());

            var formData = new ArrayList<NameValuePair>();
            formData.add(new BasicNameValuePair("login", sonarInstance.getUsername()));
            formData.add(
                    new BasicNameValuePair("password", sonarInstance.getPass().getPlainText()));
            formData.add(new BasicNameValuePair("remember_me", "1"));
            loginHttpPost.setEntity(createEntity(formData));
            loginHttpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

            executePostRequest(httpClient, loginHttpPost);
        }

        logged = true;
    }

    protected abstract String getSonarApiLogin();

    private void executePostRequest(CloseableHttpClient client, HttpPost loginHttpPost) throws QGException {
        try {
            client.execute(loginHttpPost, classicHttpResponse -> classicHttpResponse);
        } catch (IOException e) {
            throw new QGException("POST execution error", e);
        }
    }

    private UrlEncodedFormEntity createEntity(List<NameValuePair> nvps) throws QGException {
        return new UrlEncodedFormEntity(nvps);
    }

    private String executeGetRequest(CloseableHttpClient client, HttpGet request) throws QGException {
        if (StringUtils.isNotEmpty(token)) {
            String authHeader =
                    "Basic " + Base64.getEncoder().encodeToString((token + ":").getBytes(StandardCharsets.UTF_8));

            request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }

        try (var response = client.execute(request, httpClientContext, classicHttpResponse -> classicHttpResponse)) {
            var statusCode = response.getCode();
            var entity = response.getEntity();
            var returnResponse = EntityUtils.toString(entity);

            EntityUtils.consume(entity);

            if (statusCode != 200) {
                throw new QGException("Expected status 200, got: " + statusCode + ". Response: " + returnResponse);
            }

            return returnResponse;
        } catch (IOException | ParseException e) {
            throw new QGException("GET execution error", e);
        }
    }

    String getAPITaskInfo(JobConfigData configData, SonarInstance sonarInstance) throws QGException {
        checkLogged(sonarInstance);

        var sonarProjectKey = getSonarApiTaskInfoParameter(configData, sonarInstance);
        var sonarProjectTaskInfoPath = getSonarApiTaskInfoUrl();
        var sonarHostUrl = sonarInstance.getUrl();
        var taskInfoUri =
                sonarHostUrl + sonarProjectTaskInfoPath.formatted(encode(sonarProjectKey, StandardCharsets.UTF_8));
        var request = new HttpGet(taskInfoUri);

        return executeGetRequest(httpClient, request);
    }

    protected abstract String getSonarApiTaskInfoUrl();

    protected abstract String getSonarApiTaskInfoParameter(JobConfigData jobConfigData, SonarInstance sonarInstance);

    String getAPIInfo(JobConfigData configData, SonarInstance sonarInstance) throws QGException {
        checkLogged(sonarInstance);

        var sonarApiQualityGates =
                sonarInstance.getUrl() + getSonarApiQualityGatesStatusUrl().formatted(configData.getProjectKey());
        var request = new HttpGet(sonarApiQualityGates.formatted(configData.getProjectKey()));

        return executeGetRequest(httpClient, request);
    }

    protected abstract String getSonarApiQualityGatesStatusUrl();

    protected String getComponentId(JobConfigData configData, SonarInstance sonarInstance) {
        checkLogged(sonarInstance);

        var sonarApiQualityGates =
                sonarInstance.getUrl() + getSonarApiComponentShow().formatted(configData.getProjectKey());
        var request = new HttpGet(sonarApiQualityGates);
        var result = executeGetRequest(httpClient, request);
        var gson = new GsonBuilder().create();
        var component = gson.fromJson(result, SonarComponentShow.class);

        return component.getComponent().getId();
    }

    private void checkLogged(SonarInstance sonarInstance) {
        if (!isLogged()) {
            loginApi(sonarInstance);
        }
    }
}
