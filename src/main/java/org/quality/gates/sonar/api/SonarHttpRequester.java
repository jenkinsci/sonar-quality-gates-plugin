package org.quality.gates.sonar.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.jenkins.plugin.JobConfigData;
import org.quality.gates.jenkins.plugin.QGException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author arkanjoms
 * @author friesoft
 * @since 1.0.1
 */
public abstract class SonarHttpRequester {

	private static Logger LOGGER = LoggerFactory.getLogger(SonarHttpRequester.class);

	private static final String SONAR_API_COMPONENT_SHOW = "/api/components/show?key=%s";

	protected abstract String getSonarApiLogin();

	private String executeGetRequest(HttpGet request, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance)
			throws QGException {

		CloseableHttpResponse response = null;

		try {
			HttpClientContext context = HttpClientContext.create();
			CloseableHttpClient client = HttpClientBuilder.create().build();

			String token = globalConfigDataForSonarInstance.getToken();
			if (token == null || token.equals("")) {
				throw new QGException("No token specified");
			}
			request.addHeader("Authorization", BasicScheme.authenticate(new UsernamePasswordCredentials(token, ""), "UTF-8"));

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
		String sonarApiTaskInfo = globalConfigDataForSonarInstance.getSonarUrl()
				+ String.format(getSonarApiTaskInfoUrl(), getSonarApiTaskInfoParameter(projectKey, globalConfigDataForSonarInstance));

		String getUrl = String.format(sonarApiTaskInfo, projectKey.getProjectKey());
		HttpGet request = new HttpGet(getUrl);

		return executeGetRequest(request, globalConfigDataForSonarInstance);
	}

	protected abstract String getSonarApiTaskInfoUrl();

	protected abstract String getSonarApiTaskInfoParameter(JobConfigData jobConfigData,
			GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance);

	String getAPIInfo(JobConfigData projectKey, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) throws QGException {
		String sonarApiQualityGates = globalConfigDataForSonarInstance.getSonarUrl()
				+ String.format(getSonarApiQualityGatesStatusUrl(), projectKey.getProjectKey());

		HttpGet request = new HttpGet(String.format(sonarApiQualityGates, projectKey.getProjectKey()));

		return executeGetRequest(request, globalConfigDataForSonarInstance);
	}

	protected abstract String getSonarApiQualityGatesStatusUrl();

	protected String getComponentId(JobConfigData configData, GlobalConfigDataForSonarInstance globalConfigData) {

		String sonarApiQualityGates = globalConfigData.getSonarUrl() + String.format(SONAR_API_COMPONENT_SHOW, configData.getProjectKey());

		HttpGet request = new HttpGet(sonarApiQualityGates);

		String result = executeGetRequest(request, globalConfigData);

		Gson gson = new GsonBuilder().create();

		SonarComponentShow component = gson.fromJson(result, SonarComponentShow.class);

		return component.getComponent().getId();
	}
}
