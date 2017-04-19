package quality.gates.sonar.api5x;

import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import quality.gates.jenkins.plugin.JobConfigData;

import java.util.ArrayList;
import java.util.List;

public class SonarHttpRequester5x extends quality.gates.sonar.api.SonarHttpRequester {

    private static final String SONAR_API_LOGIN = "/api/authentication/login";

    private static final String SONAR_API_QUALITY_GATES_STATUS = "/api/events?resource=%s&format=json&categories=Alert";

    private static final String SONAR_API_TASK_INFO = "/api/ce/component?componentKey=%s";

    public SonarHttpRequester5x() {

        context = HttpClientContext.create();
        client = HttpClientBuilder.create().build();
    }

    @Override
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

    @Override
    protected String getSonarApiLogin() {
        return SONAR_API_LOGIN;
    }

    @Override
    protected String getSonarApiTaskInfoUrl() {
        return SONAR_API_TASK_INFO;
    }

    @Override
    protected String getSonarApiQualityGatesStatusUrl() {
        return SONAR_API_QUALITY_GATES_STATUS;
    }
}
