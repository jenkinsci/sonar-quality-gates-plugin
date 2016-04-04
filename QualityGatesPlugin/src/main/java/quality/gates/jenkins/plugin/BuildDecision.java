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

    public boolean getStatus(JobConfigData jobConfigData) throws QGException {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = jobConfigData.getGlobalConfigDataForSonarInstance();
        try {
            return qualityGatesProvider.getAPIResultsForQualityGates(jobConfigData, globalConfigDataForSonarInstance).hasStatusGreen();
        } catch (JSONException e) {
            throw new QGException("Please check your credentials or your Project Key", e);
        }
    }
}