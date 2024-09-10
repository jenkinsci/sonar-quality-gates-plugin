package org.quality.gates.jenkins.plugin;

import hudson.model.BuildListener;
import org.json.JSONException;
import org.quality.gates.sonar.api.QualityGatesProvider;

public class BuildDecision {

    private final QualityGatesProvider qualityGatesProvider;

    public BuildDecision(SonarInstance sonarInstance) {
        qualityGatesProvider = new QualityGatesProvider(sonarInstance);
    }

    public BuildDecision(QualityGatesProvider qualityGatesProvider) {
        this.qualityGatesProvider = qualityGatesProvider;
    }

    public boolean getStatus(SonarInstance sonarInstance, JobConfigData jobConfigData, BuildListener listener)
            throws QGException {
        try {
            return qualityGatesProvider
                    .getAPIResultsForQualityGates(jobConfigData, sonarInstance, listener)
                    .hasStatusGreen();
        } catch (JSONException | InterruptedException e) {
            throw new QGException("Please check your credentials or your Project Key", e);
        }
    }

    SonarInstance chooseSonarInstance(GlobalSonarQualityGatesConfiguration globalConfig, JobConfigData jobConfigData) {
        if (globalConfig.fetchSonarInstances().isEmpty()) {
            return noSonarInstance(jobConfigData);
        } else if (globalConfig.fetchSonarInstances().size() == 1) {
            return singleSonarInstance(globalConfig, jobConfigData);
        }

        return multipleSonarInstances(jobConfigData.getSonarInstanceName(), globalConfig);
    }

    private SonarInstance noSonarInstance(JobConfigData jobConfigData) {
        jobConfigData.setSonarInstanceName("");
        return new SonarInstance();
    }

    private SonarInstance singleSonarInstance(
            GlobalSonarQualityGatesConfiguration globalConfig, JobConfigData jobConfigData) {
        var sonarInstance = globalConfig.fetchSonarInstances().get(0);
        jobConfigData.setSonarInstanceName(sonarInstance.getName());

        return sonarInstance;
    }

    public SonarInstance multipleSonarInstances(
            String instanceName, GlobalSonarQualityGatesConfiguration globalConfig) {
        return globalConfig.getSonarInstanceByName(instanceName);
    }
}
