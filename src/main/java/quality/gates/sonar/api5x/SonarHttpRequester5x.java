package quality.gates.sonar.api5x;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import quality.gates.jenkins.plugin.JobConfigData;
import quality.gates.sonar.api.SonarHttpRequester;

public class SonarHttpRequester5x extends SonarHttpRequester {
    //    http://localhost:9500/api/ce/component?componentId=AVuHVFu-IMCcK-xt18YB
//
//
//
//    http://localhost:9500/api/components/show?key=org.jenkins-ci.plugins:qa-plugin-sonar
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

    @Override
    protected String getSonarApiTaskInfoParameter(JobConfigData jobConfigData,GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

//        if (!isNeedSearchId()) {
//            parameterValue = projectKey.getProjectKey();
//        } else {
            return getComponentId(jobConfigData, globalConfigDataForSonarInstance);
//        }
    }
}
