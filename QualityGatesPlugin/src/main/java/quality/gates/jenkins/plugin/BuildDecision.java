package quality.gates.jenkins.plugin;

import quality.gates.sonar.api.QualityGatesProvider;
import org.json.JSONException;

public class BuildDecision {

    private QualityGatesProvider qualityGatesProvider;

    public BuildDecision() {
        qualityGatesProvider = new QualityGatesProvider();
    }

    public BuildDecision(QualityGatesProvider qualityGatesProvider) {
        this.qualityGatesProvider = qualityGatesProvider;
    }

    public boolean getStatus(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance, JobConfigData jobConfigData) throws QGException {
        try {
            return qualityGatesProvider.getAPIResultsForQualityGates(jobConfigData, globalConfigDataForSonarInstance).hasStatusGreen();
        } catch (JSONException e) {
            throw new QGException("Please check your credentials or your Project Key", e);
        }
    }

    public GlobalConfigDataForSonarInstance chooseSonarInstance (GlobalConfig globalConfig, String instanceName) {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;
        if (globalConfig.fetchListOfGlobalConfigData().isEmpty()) {
            globalConfigDataForSonarInstance = noSonarInstance();
        }
        else if (globalConfig.fetchListOfGlobalConfigData().size() == 1) {
            globalConfigDataForSonarInstance = singleSonarInstance(globalConfig);
        }
        else {
            globalConfigDataForSonarInstance = multipleSonarInstances(instanceName, globalConfig);
        }
        return globalConfigDataForSonarInstance;
    }

    public GlobalConfigDataForSonarInstance noSonarInstance() {
        return new GlobalConfigDataForSonarInstance();
    }

    public GlobalConfigDataForSonarInstance singleSonarInstance(GlobalConfig globalConfig) {
        return globalConfig.fetchListOfGlobalConfigData().get(0);
    }

    public GlobalConfigDataForSonarInstance multipleSonarInstances(String instanceName, GlobalConfig globalConfig) {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = globalConfig.getSonarInstanceByName(instanceName);
        if(globalConfigDataForSonarInstance != null) {
            return globalConfigDataForSonarInstance;
        }
        else
            return null;
    }
}