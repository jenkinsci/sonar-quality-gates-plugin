package org.quality.gates.sonar.api88;

import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.jenkins.plugin.JobConfigData;
import org.quality.gates.sonar.api.SonarHttpRequester;

/**
 * @author lumi
 * @since 1.3.2
 */
public class SonarHttpRequester88 extends SonarHttpRequester {

    private static final String SONAR_API_LOGIN = "/api/authentication/login";

    private static final String SONAR_API_QUALITY_GATES_STATUS = "/api/qualitygates/project_status?projectKey=%s";

    private static final String SONAR_API_TASK_INFO = "/api/ce/component?component=%s";

    private static final String SONAR_API_COMPONENT_SHOW = "/api/components/show?component=%s";
    
    public SonarHttpRequester88() {
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
    protected String getSonarApiComponentShow() {
        return SONAR_API_COMPONENT_SHOW;
    }
    
    @Override
    protected String getSonarApiTaskInfoParameter(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        return jobConfigData.getProjectKey();
    }
}

