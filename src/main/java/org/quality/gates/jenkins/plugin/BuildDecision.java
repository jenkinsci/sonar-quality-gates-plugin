package org.quality.gates.jenkins.plugin;

import hudson.model.BuildListener;
import org.json.JSONException;
import org.quality.gates.sonar.api.QualityGatesProvider;

public class BuildDecision {

    private final QualityGatesProvider qualityGatesProvider;

    public BuildDecision(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        qualityGatesProvider = new QualityGatesProvider(globalConfigDataForSonarInstance);
    }

    public BuildDecision(QualityGatesProvider qualityGatesProvider) {
        this.qualityGatesProvider = qualityGatesProvider;
    }

    public boolean getStatus(
            GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance,
            JobConfigData jobConfigData,
            BuildListener listener)
            throws QGException {
        try {
            return qualityGatesProvider
                    .getAPIResultsForQualityGates(jobConfigData, globalConfigDataForSonarInstance, listener)
                    .hasStatusGreen();
        } catch (JSONException | InterruptedException e) {
            throw new QGException("Please check your credentials or your Project Key", e);
        }
    }

    GlobalConfigDataForSonarInstance chooseSonarInstance(GlobalConfig globalConfig, JobConfigData jobConfigData) {
        if (globalConfig.fetchListOfGlobalConfigData().isEmpty()) {
            return noSonarInstance(jobConfigData);
        } else if (globalConfig.fetchListOfGlobalConfigData().size() == 1) {
            return singleSonarInstance(globalConfig, jobConfigData);
        }

        return multipleSonarInstances(jobConfigData.getSonarInstanceName(), globalConfig);
    }

    private GlobalConfigDataForSonarInstance noSonarInstance(JobConfigData jobConfigData) {
        jobConfigData.setSonarInstanceName("");
        return new GlobalConfigDataForSonarInstance();
    }

    private GlobalConfigDataForSonarInstance singleSonarInstance(
            GlobalConfig globalConfig, JobConfigData jobConfigData) {
        var globalConfigDataForSonarInstance =
                globalConfig.fetchListOfGlobalConfigData().get(0);
        jobConfigData.setSonarInstanceName(globalConfigDataForSonarInstance.getName());

        return globalConfigDataForSonarInstance;
    }

    public GlobalConfigDataForSonarInstance multipleSonarInstances(String instanceName, GlobalConfig globalConfig) {
        return globalConfig.getSonarInstanceByName(instanceName);
    }
}
