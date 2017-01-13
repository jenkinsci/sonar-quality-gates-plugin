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

    private static final int MILLISECONDS_10_MINUTES = 600000;

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

        int attemptsToRepeat = jobConfigData.getAttemptsToRepeat();
        int timeToWait = globalConfigDataForSonarInstance.getTimeToWait();

        // FIXME
        if (attemptsToRepeat * timeToWait > MILLISECONDS_10_MINUTES) {//10 min
            attemptsToRepeat = MILLISECONDS_10_MINUTES / timeToWait;
        }

        int timesExecuted = 0;

        do {

            String statusResultJson = sonarHttpRequester.getAPITaskInfo(jobConfigData, validatedData);

            Gson gson = new GsonBuilder().create();

            QualityGateTaskCE taskCE = gson.fromJson(statusResultJson, QualityGateTaskCE.class);

            if (ArrayUtils.isNotEmpty(taskCE.getQueue())) {

                listener.getLogger().println("Has build " + taskCE.getQueue()[0].getStatus() + " with id: " + taskCE.getQueue()[0].getId() + " - waiting " + timeToWait + " to execute next check.");

                Thread.sleep(timeToWait);
            } else {
                if ("SUCCESS".equals(taskCE.getCurrent().getStatus())) {
                    taskAnalysisRunning = false;
                }
            }

            if (attemptsToRepeat < timesExecuted++) {
                taskAnalysisRunning = false; // FIXME throw exception
            }
        } while (taskAnalysisRunning);

        String requesterResult = getRequesterResult(jobConfigData, validatedData);

        return qualityGateResponseParser.getQualityGateResultFromJSON(requesterResult);
    }

    private String getRequesterResult(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) throws QGException {

        return sonarHttpRequester.getAPIInfo(jobConfigData, globalConfigDataForSonarInstance);
    }
}
