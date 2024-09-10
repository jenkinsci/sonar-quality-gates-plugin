package org.quality.gates.sonar.api;

import com.google.gson.GsonBuilder;
import hudson.model.BuildListener;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;
import org.quality.gates.jenkins.plugin.JobConfigData;
import org.quality.gates.jenkins.plugin.QGException;
import org.quality.gates.jenkins.plugin.SonarInstance;

public class QualityGatesProvider {

    private static final int MILLISECONDS_5_MINUTES = 300000;

    private static final int MILLISECONDS_10_SECONDS = 10000;

    private final QualityGateResponseParser qualityGateResponseParser;

    private final SonarHttpRequester sonarHttpRequester;

    private final SonarInstanceValidationService sonarInstanceValidationService;

    public QualityGatesProvider(SonarInstance sonarInstance) {
        this.qualityGateResponseParser = new QualityGateResponseParser();
        this.sonarHttpRequester = SonarHttpRequesterFactory.getSonarHttpRequester(sonarInstance);
        this.sonarInstanceValidationService = new SonarInstanceValidationService();
    }

    public QualityGatesProvider(
            QualityGateResponseParser qualityGateResponseParser,
            SonarHttpRequester sonarHttpRequester,
            SonarInstanceValidationService sonarInstanceValidationService) {
        this.qualityGateResponseParser = qualityGateResponseParser;
        this.sonarHttpRequester = sonarHttpRequester;
        this.sonarInstanceValidationService = sonarInstanceValidationService;
    }

    public QualityGatesStatus getAPIResultsForQualityGates(
            JobConfigData jobConfigData, SonarInstance sonarInstance, BuildListener listener)
            throws JSONException, InterruptedException {
        var validatedData = sonarInstanceValidationService.validateData(sonarInstance);
        var taskAnalysisRunning = true;
        var timeToWait = sonarInstance.getTimeToWait();
        var maxWaitTime = sonarInstance.getMaxWaitTime();

        if (timeToWait == 0) {
            timeToWait = MILLISECONDS_10_SECONDS;
        }

        if (maxWaitTime == 0) {
            maxWaitTime = MILLISECONDS_5_MINUTES;
        }

        var startTime = System.currentTimeMillis();

        do {
            sonarHttpRequester.setLogged(false);
            var statusResultJson = sonarHttpRequester.getAPITaskInfo(jobConfigData, validatedData);
            var gson = new GsonBuilder().create();
            var taskCE = gson.fromJson(statusResultJson, QualityGateTaskCE.class);

            if (ArrayUtils.isNotEmpty(taskCE.getQueue())) {
                listener.getLogger()
                        .println("Has build " + taskCE.getQueue()[0].getStatus() + " with id: "
                                + taskCE.getQueue()[0].getId() + " - waiting " + timeToWait
                                + " to execute next check. DEBUG:" + (System.currentTimeMillis() - startTime));

                Thread.sleep(timeToWait);
            } else {
                listener.getLogger().println("Status => " + taskCE.getCurrent().getStatus());

                if ("SUCCESS".equals(taskCE.getCurrent().getStatus())) {
                    taskAnalysisRunning = false;
                }
            }

            if ((System.currentTimeMillis() - startTime) > maxWaitTime) {
                throw new MaxExecutionTimeException("Status => Max time to wait sonar job!");
            }
        } while (taskAnalysisRunning);

        var requesterResult = getRequesterResult(jobConfigData, validatedData);

        return qualityGateResponseParser.getQualityGateResultFromJSON(requesterResult);
    }

    private String getRequesterResult(JobConfigData jobConfigData, SonarInstance sonarInstance) throws QGException {
        return sonarHttpRequester.getAPIInfo(jobConfigData, sonarInstance);
    }
}
