package quality.gates.jenkins.plugin;

import hudson.model.BuildListener;
import org.json.JSONException;
import quality.gates.sonar.api.QualityGatesProvider;

public class BuildDecision {

    private QualityGatesProvider qualityGatesProvider;

    public BuildDecision() {
        qualityGatesProvider = new QualityGatesProvider();
    }

    public BuildDecision(QualityGatesProvider qualityGatesProvider) {
        this.qualityGatesProvider = qualityGatesProvider;
    }

    public boolean getStatus(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance, JobConfigData jobConfigData, BuildListener listener) throws QGException {

        try {
            return qualityGatesProvider.getAPIResultsForQualityGates(jobConfigData, globalConfigDataForSonarInstance, listener).hasStatusGreen();
        } catch (JSONException | InterruptedException e) {
            throw new QGException("Please check your credentials or your Project Key", e);
        }
    }

    GlobalConfigDataForSonarInstance chooseSonarInstance(GlobalConfig globalConfig, JobConfigData jobConfigData) {

        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

        if (globalConfig.fetchListOfGlobalConfigData().isEmpty()) {
            globalConfigDataForSonarInstance = noSonarInstance(jobConfigData);
        } else if (globalConfig.fetchListOfGlobalConfigData().size() == 1) {
            globalConfigDataForSonarInstance = singleSonarInstance(globalConfig, jobConfigData);
        } else {
            globalConfigDataForSonarInstance = multipleSonarInstances(jobConfigData.getSonarInstanceName(), globalConfig);
        }

        return globalConfigDataForSonarInstance;
    }

    private GlobalConfigDataForSonarInstance noSonarInstance(JobConfigData jobConfigData) {

        jobConfigData.setSonarInstanceName("");
        return new GlobalConfigDataForSonarInstance();
    }

    private GlobalConfigDataForSonarInstance singleSonarInstance(GlobalConfig globalConfig, JobConfigData jobConfigData) {

        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = globalConfig.fetchListOfGlobalConfigData().get(0);
        jobConfigData.setSonarInstanceName(globalConfigDataForSonarInstance.getName());
        return globalConfigDataForSonarInstance;
    }

    public GlobalConfigDataForSonarInstance multipleSonarInstances(String instanceName, GlobalConfig globalConfig) {

        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = globalConfig.getSonarInstanceByName(instanceName);

        if (globalConfigDataForSonarInstance != null) {
            return globalConfigDataForSonarInstance;
        }

        return null;
    }
}
