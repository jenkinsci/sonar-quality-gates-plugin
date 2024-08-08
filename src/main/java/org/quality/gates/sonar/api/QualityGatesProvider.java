package org.quality.gates.sonar.api;

import static org.apache.commons.lang.ArrayUtils.isNotEmpty;

import com.google.gson.GsonBuilder;
import hudson.model.BuildListener;
import hudson.model.Run;
import org.json.JSONException;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.jenkins.plugin.JobConfigData;
import org.quality.gates.jenkins.plugin.QGException;

public class QualityGatesProvider {

    private static final int FIVE_MINUTES_IN_MILLISECONDS = 300000;

    private static final int TEN_SECONDS_IN_MILLISECONDS = 10000;

    private final QualityGateResponseParser qualityGateResponseParser;

    private final SonarHttpRequester sonarHttpRequester;

    private final SonarInstanceValidationService sonarInstanceValidationService;

    public QualityGatesProvider(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        this.qualityGateResponseParser = new QualityGateResponseParser();
        this.sonarHttpRequester = SonarHttpRequesterFactory.getSonarHttpRequester(globalConfigDataForSonarInstance);
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
            JobConfigData jobConfigData,
            GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance,
            BuildListener listener,
            Run<?, ?> run)
            throws JSONException, InterruptedException {
        var validatedData = sonarInstanceValidationService.validateData(globalConfigDataForSonarInstance);
        var taskAnalysisRunning = true;
        var intervalTimeToWait = getIntervalTimeToWait(globalConfigDataForSonarInstance);
        var maxWaitTime = getMaxWaitTime(globalConfigDataForSonarInstance);

        var startTime = System.currentTimeMillis();

        do {
            var statusResultJson = sonarHttpRequester.getAPITaskInfo(jobConfigData, validatedData, run);
            var gson = new GsonBuilder().create();
            var taskCE = gson.fromJson(statusResultJson, QualityGateTaskCE.class);

            if (isNotEmpty(taskCE.getQueue())) {
                listener.getLogger()
                        .println("Has build " + taskCE.getQueue()[0].getStatus() + " with id: "
                                + taskCE.getQueue()[0].getId() + " - waiting " + intervalTimeToWait
                                + " to execute next check. DEBUG:" + (System.currentTimeMillis() - startTime));

                Thread.sleep(intervalTimeToWait);
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

        var requesterResult = getRequesterResult(jobConfigData, validatedData, run);

        return qualityGateResponseParser.getQualityGateResultFromJSON(requesterResult);
    }

    private int getIntervalTimeToWait(GlobalConfigDataForSonarInstance config) {
        var intervalTimeToWait = config.getTimeToWait();
        return intervalTimeToWait != 0 ? intervalTimeToWait : TEN_SECONDS_IN_MILLISECONDS;
    }

    private int getMaxWaitTime(GlobalConfigDataForSonarInstance config) {
        var maxWaitTime = config.getMaxWaitTime();
        return maxWaitTime != 0 ? maxWaitTime : FIVE_MINUTES_IN_MILLISECONDS;
    }

    private String getRequesterResult(
            JobConfigData jobConfigData,
            GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance,
            Run<?, ?> run)
            throws QGException {
        return sonarHttpRequester.getAPIInfo(jobConfigData, globalConfigDataForSonarInstance, run);
    }
}
