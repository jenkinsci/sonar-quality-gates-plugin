package org.quality.gates.sonar.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.jenkins.plugin.JobConfigData;
import org.quality.gates.jenkins.plugin.QGException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
public abstract class SonarHttpRequester {

    private static Logger LOGGER = LoggerFactory.getLogger(SonarHttpRequester.class);

    private static final String SONAR_API_COMPONENT_SHOW = "/api/components/show?key=%s";

    /**
     * Cached client context for lazy login.
     * @see #loginApi(GlobalConfigDataForSonarInstance)
     */
    private transient HttpClientContext httpClientContext;

    /**
     * Cached client for lazy login.
     * @see #loginApi(GlobalConfigDataForSonarInstance)
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

    private void loginApi(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        httpClientContext = HttpClientContext.create();

        if (StringUtils.isNotEmpty(globalConfigDataForSonarInstance.getToken().getPlainText())) {
            token = globalConfigDataForSonarInstance.getToken().getPlainText();
            httpClient = HttpClientBuilder.create().build();
        } else {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            globalConfigDataForSonarInstance.getUsername(),
                            globalConfigDataForSonarInstance.getPass().getPlainText()));

            httpClient = HttpClientBuilder.create()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();

            httpClientContext.setCredentialsProvider(credsProvider);

            HttpPost loginHttpPost = new HttpPost(globalConfigDataForSonarInstance.getSonarUrl() + getSonarApiLogin());

            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("login", globalConfigDataForSonarInstance.getUsername()));
            nvps.add(new BasicNameValuePair(
                    "password", globalConfigDataForSonarInstance.getPass().getPlainText()));
            nvps.add(new BasicNameValuePair("remember_me", "1"));
            loginHttpPost.setEntity(createEntity(nvps));
            loginHttpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

            executePostRequest(httpClient, loginHttpPost);
        }

        logged = true;
    }

    protected abstract String getSonarApiLogin();

    private void executePostRequest(CloseableHttpClient client, HttpPost loginHttpPost) throws QGException {

        try {
            client.execute(loginHttpPost);
        } catch (IOException e) {
            throw new QGException("POST execution error", e);
        }
    }

    private UrlEncodedFormEntity createEntity(List<NameValuePair> nvps) throws QGException {

        try {
            return new UrlEncodedFormEntity(nvps);
        } catch (UnsupportedEncodingException e) {
            throw new QGException("Encoding error", e);
        }
    }

    private String executeGetRequest(CloseableHttpClient client, HttpGet request) throws QGException {

        CloseableHttpResponse response = null;

        try {
            if (StringUtils.isNotEmpty(token)) {
                String authHeader =
                        "Basic " + Base64.getEncoder().encodeToString((token + ":").getBytes(StandardCharsets.UTF_8));

                request.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
            }
            response = client.execute(request, httpClientContext);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String returnResponse = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            if (statusCode != 200) {
                throw new QGException("Expected status 200, got: " + statusCode + ". Response: " + returnResponse);
            }

            return returnResponse;
        } catch (IOException e) {
            throw new QGException("GET execution error", e);
        } finally {
            try {
                if (response != null) response.close();
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    String getAPITaskInfo(JobConfigData configData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance)
            throws QGException {

        checkLogged(globalConfigDataForSonarInstance);

        try {
            String sonarProjectKey = getSonarApiTaskInfoParameter(configData, globalConfigDataForSonarInstance);
            String sonarProjectTaskInfoPath = getSonarApiTaskInfoUrl();
            String sonarHostUrl = globalConfigDataForSonarInstance.getSonarUrl();
            String taskInfoUri =
                    sonarHostUrl + String.format(sonarProjectTaskInfoPath, URLEncoder.encode(sonarProjectKey, "UTF-8"));

            HttpGet request = new HttpGet(taskInfoUri);
            return executeGetRequest(httpClient, request);
        } catch (UnsupportedEncodingException e) {
            throw new QGException(e);
        }
    }

    protected abstract String getSonarApiTaskInfoUrl();

    protected abstract String getSonarApiTaskInfoParameter(
            JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance);

    String getAPIInfo(JobConfigData configData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance)
            throws QGException {

        checkLogged(globalConfigDataForSonarInstance);

        String sonarApiQualityGates = globalConfigDataForSonarInstance.getSonarUrl()
                + String.format(getSonarApiQualityGatesStatusUrl(), configData.getProjectKey());

        HttpGet request = new HttpGet(String.format(sonarApiQualityGates, configData.getProjectKey()));

        return executeGetRequest(httpClient, request);
    }

    protected abstract String getSonarApiQualityGatesStatusUrl();

    protected String getComponentId(
            JobConfigData configData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        checkLogged(globalConfigDataForSonarInstance);

        String sonarApiQualityGates = globalConfigDataForSonarInstance.getSonarUrl()
                + String.format(getSonarApiComponentShow(), configData.getProjectKey());

        HttpGet request = new HttpGet(sonarApiQualityGates);

        String result = executeGetRequest(httpClient, request);

        Gson gson = new GsonBuilder().create();

        SonarComponentShow component = gson.fromJson(result, SonarComponentShow.class);

        return component.getComponent().getId();
    }

    private void checkLogged(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        if (!isLogged()) {
            loginApi(globalConfigDataForSonarInstance);
        }
    }
}
