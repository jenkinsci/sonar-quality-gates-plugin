package quality.gates.sonar.api;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.model.BuildListener;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;
import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import quality.gates.jenkins.plugin.JobConfigData;
import quality.gates.jenkins.plugin.QGException;

public class QualityGatesProvider {

    private QualityGateResponseParser qualityGateResponseParser;

    private SonarHttpRequester sonarHttpRequester;

    private SonarInstanceValidationService sonarInstanceValidationService;

    public QualityGatesProvider() {

        this.qualityGateResponseParser = new QualityGateResponseParser();
        this.sonarHttpRequester = new SonarHttpRequester();
        this.sonarInstanceValidationService = new SonarInstanceValidationService();
    }

    public QualityGatesProvider(QualityGateResponseParser qualityGateResponseParser, SonarHttpRequester sonarHttpRequester, SonarInstanceValidationService sonarInstanceValidationService) {

        this.qualityGateResponseParser = qualityGateResponseParser;
        this.sonarHttpRequester = sonarHttpRequester;
        this.sonarInstanceValidationService = sonarInstanceValidationService;
    }

    public QualityGatesStatus getAPIResultsForQualityGates(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance, BuildListener listener) throws JSONException, InterruptedException {

        GlobalConfigDataForSonarInstance validatedData = sonarInstanceValidationService.validateData(globalConfigDataForSonarInstance);

        boolean taskAnalysisRunning = true;

        do {

            String statusResultJson = sonarHttpRequester.getAPITaskInfo(jobConfigData, validatedData);

            Gson gson = new GsonBuilder().create();

            QualityGateTaskCE taskCE = gson.fromJson(statusResultJson, QualityGateTaskCE.class);

            if (ArrayUtils.isNotEmpty(taskCE.getQueue())) {

                listener.getLogger().println("Has build " + taskCE.getQueue()[0].getStatus() + " with id: " + taskCE.getQueue()[0].getId());

                Thread.sleep(7500);// TODO add sleep time to parameter
            } else {
                if ("SUCCESS".equals(taskCE.getCurrent().getStatus())) {
                    taskAnalysisRunning = false;
                }
            }
        } while (taskAnalysisRunning);

        String requesterResult = getRequesterResult(jobConfigData, validatedData);

        return qualityGateResponseParser.getQualityGateResultFromJSON(requesterResult);
    }

    public String getRequesterResult(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) throws QGException {

        return sonarHttpRequester.getAPIInfo(jobConfigData, globalConfigDataForSonarInstance);
    }
}
