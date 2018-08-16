package org.quality.gates.sonar.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.model.BuildListener;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.jenkins.plugin.JobConfigData;
import org.quality.gates.jenkins.plugin.QGException;

import java.io.UnsupportedEncodingException;

public class QualityGatesProvider {

    private static final int MILLISECONDS_5_MINUTES = 300000;

    private static final int MILLISECONDS_10_SECONDS = 10000;

    private QualityGateResponseParser qualityGateResponseParser;

    private SonarHttpRequester sonarHttpRequester;

    private SonarInstanceValidationService sonarInstanceValidationService;

    public QualityGatesProvider(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        this.qualityGateResponseParser = new QualityGateResponseParser();
        this.sonarHttpRequester = SonarHttpRequesterFactory.getSonarHttpRequester(globalConfigDataForSonarInstance);
        this.sonarInstanceValidationService = new SonarInstanceValidationService();
    }

    public QualityGatesProvider(QualityGateResponseParser qualityGateResponseParser, SonarHttpRequester sonarHttpRequester, SonarInstanceValidationService sonarInstanceValidationService) {

        this.qualityGateResponseParser = qualityGateResponseParser;
        this.sonarHttpRequester = sonarHttpRequester;
        this.sonarInstanceValidationService = sonarInstanceValidationService;
    }

    public QualityGatesStatus getAPIResultsForQualityGates(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance, BuildListener listener) throws JSONException, InterruptedException{

        GlobalConfigDataForSonarInstance validatedData = sonarInstanceValidationService.validateData(globalConfigDataForSonarInstance);

        boolean taskAnalysisRunning = true;

        int timeToWait = globalConfigDataForSonarInstance.getTimeToWait();
        int maxWaitTime = globalConfigDataForSonarInstance.getMaxWaitTime();

        if (timeToWait == 0) {
            timeToWait = MILLISECONDS_10_SECONDS;
        }

        if (maxWaitTime == 0) {
            maxWaitTime = MILLISECONDS_5_MINUTES;
        }

        long startTime = System.currentTimeMillis();

        do {

            sonarHttpRequester.setLogged(false);
            String statusResultJson = sonarHttpRequester.getAPITaskInfo(jobConfigData, validatedData);

            Gson gson = new GsonBuilder().create();

            QualityGateTaskCE taskCE = gson.fromJson(statusResultJson, QualityGateTaskCE.class);

            if (ArrayUtils.isNotEmpty(taskCE.getQueue())) {

                listener.getLogger().println("Has build " + taskCE.getQueue()[0].getStatus() + " with id: " + taskCE.getQueue()[0].getId() + " - waiting " + timeToWait + " to execute next check. DEBUG:" + (System.currentTimeMillis()-startTime));

                Thread.sleep(timeToWait);
            } else {
                listener.getLogger().println("Status => " + taskCE.getCurrent().getStatus());

                if ("SUCCESS".equals(taskCE.getCurrent().getStatus())) {
                    taskAnalysisRunning = false;
                }
            }

            if ((System.currentTimeMillis()-startTime)>maxWaitTime) {
                throw new MaxExecutionTimeException("Status => Max time to wait sonar job!");
            }
        } while (taskAnalysisRunning);

        String requesterResult = getRequesterResult(jobConfigData, validatedData);

        return qualityGateResponseParser.getQualityGateResultFromJSON(requesterResult);
    }

    private String getRequesterResult(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) throws QGException {

        return sonarHttpRequester.getAPIInfo(jobConfigData, globalConfigDataForSonarInstance);
    }
}
