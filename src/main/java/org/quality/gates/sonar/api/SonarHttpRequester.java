package org.quality.gates.sonar.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.jenkins.plugin.JobConfigData;
import org.quality.gates.jenkins.plugin.QGException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
public abstract class SonarHttpRequester {

    private static Logger LOGGER = LoggerFactory.getLogger(SonarHttpRequester.class);

    private static final String SONAR_API_COMPONENT_SHOW = "/api/components/show?key=%s";

    protected HttpClientContext context;

    protected CloseableHttpClient client;

    protected boolean logged = false;

    protected void loginApi(JobConfigData projectKey, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        HttpPost loginHttpPost = new HttpPost(globalConfigDataForSonarInstance.getSonarUrl() + getSonarApiLogin());

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("login", globalConfigDataForSonarInstance.getUsername()));
        nvps.add(new BasicNameValuePair("password", globalConfigDataForSonarInstance.getPass()));
        nvps.add(new BasicNameValuePair("remember_me", "1"));
        loginHttpPost.setEntity(createEntity(nvps));
        loginHttpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

        executePostRequest(client, loginHttpPost);

        logged = true;
    }

    protected abstract String getSonarApiLogin();

    protected void executePostRequest(CloseableHttpClient client, HttpPost loginHttpPost) throws QGException {

        try {
            client.execute(loginHttpPost);
        } catch (IOException e) {
            throw new QGException("POST execution error", e);
        }
    }

    protected UrlEncodedFormEntity createEntity(List<NameValuePair> nvps) throws QGException {

        try {
            return new UrlEncodedFormEntity(nvps);
        } catch (UnsupportedEncodingException e) {
            throw new QGException("Encoding error", e);
        }
    }

    private String executeGetRequest(CloseableHttpClient client, HttpGet request) throws QGException {

        CloseableHttpResponse response = null;

        try {
            response = client.execute(request, context);
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
                if (response != null)
                    response.close();
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    String getAPITaskInfo(JobConfigData projectKey, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) throws QGException {

        if (!logged) {
            loginApi(projectKey, globalConfigDataForSonarInstance);
        }

        String sonarApiTaskInfo = globalConfigDataForSonarInstance.getSonarUrl() + String.format(getSonarApiTaskInfoUrl(), getSonarApiTaskInfoParameter(projectKey, globalConfigDataForSonarInstance));

        String getUrl = String.format(sonarApiTaskInfo, projectKey.getProjectKey());
        HttpGet request = new HttpGet(getUrl);

        return executeGetRequest(client, request);
    }

    protected abstract String getSonarApiTaskInfoUrl();

    protected abstract String getSonarApiTaskInfoParameter(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance);

    public String getAPIInfo(JobConfigData projectKey, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) throws QGException {

        if (!logged) {
            loginApi(projectKey, globalConfigDataForSonarInstance);
        }

        String sonarApiQualityGates = globalConfigDataForSonarInstance.getSonarUrl() + String.format(getSonarApiQualityGatesStatusUrl(), projectKey.getProjectKey());

        HttpGet request = new HttpGet(String.format(sonarApiQualityGates, projectKey.getProjectKey()));

        return executeGetRequest(client, request);
    }

    protected abstract String getSonarApiQualityGatesStatusUrl();

    public String getComponentId(JobConfigData configData, GlobalConfigDataForSonarInstance globalConfigData) {

        if (!logged) {
            loginApi(configData, globalConfigData);
        }

        String sonarApiQualityGates = globalConfigData.getSonarUrl() + String.format(SONAR_API_COMPONENT_SHOW, configData.getProjectKey());

        HttpGet request = new HttpGet(sonarApiQualityGates);

        String result = executeGetRequest(client, request);

        Gson gson = new GsonBuilder().create();

        SonarComponentShow component = gson.fromJson(result, SonarComponentShow.class);

        return component.getComponent().getId();
    }
}
