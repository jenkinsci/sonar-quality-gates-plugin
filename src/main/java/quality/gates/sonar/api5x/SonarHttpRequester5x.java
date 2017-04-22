package quality.gates.sonar.api5x;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import quality.gates.sonar.api.SonarHttpRequester;

public class SonarHttpRequester5x extends SonarHttpRequester {

    private static final String SONAR_API_LOGIN = "/sessions/login";

    private static final String SONAR_API_QUALITY_GATES_STATUS = "/api/qualitygates/project_status?projectKey=%s";

    private static final String SONAR_API_TASK_INFO = "/api/ce/component?componentId=%s";

    public SonarHttpRequester5x() {

        context = HttpClientContext.create();
        client = HttpClientBuilder.create().build();
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
