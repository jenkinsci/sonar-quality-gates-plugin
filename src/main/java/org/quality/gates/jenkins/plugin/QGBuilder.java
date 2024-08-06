package org.quality.gates.jenkins.plugin;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.Builder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.quality.gates.jenkins.plugin.enumeration.BuildStatusEnum;

public class QGBuilder extends Builder {

    private JobConfigData jobConfigData;

    private BuildDecision buildDecision;

    private JobConfigurationService jobConfigurationService;

    private JobExecutionService jobExecutionService;

    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @DataBoundConstructor
    public QGBuilder(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        this.jobConfigData = jobConfigData;
        this.buildDecision = new BuildDecision(globalConfigDataForSonarInstance);
        this.jobExecutionService = new JobExecutionService();
        this.jobConfigurationService = new JobConfigurationService();
        this.globalConfigDataForSonarInstance = null;
    }

    protected QGBuilder(JobConfigData jobConfigData, BuildDecision buildDecision, JobExecutionService jobExecutionService, JobConfigurationService jobConfigurationService, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        this.jobConfigData = jobConfigData;
        this.buildDecision = buildDecision;
        this.jobExecutionService = jobExecutionService;
        this.jobConfigurationService = jobConfigurationService;
        this.globalConfigDataForSonarInstance = globalConfigDataForSonarInstance;
    }

    public JobConfigData getJobConfigData() {
        return jobConfigData;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {

        globalConfigDataForSonarInstance = buildDecision.chooseSonarInstance(jobExecutionService.getGlobalConfigData(), jobConfigData);

        if (globalConfigDataForSonarInstance == null) {
            listener.error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, jobConfigData.getSonarInstanceName());
            return false;
        }

        return true;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace(listener.getLogger());
        }

        boolean buildHasPassed = false;

        try {
            JobConfigData checkedJobConfigData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
            buildHasPassed = buildDecision.getStatus(globalConfigDataForSonarInstance, checkedJobConfigData, listener);
            if ("".equals(jobConfigData.getSonarInstanceName()))
                listener.getLogger().println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
            listener.getLogger().println("Build-Step: Quality Gates plugin build passed: " + String.valueOf(buildHasPassed).toUpperCase());

            if (!buildHasPassed && BuildStatusEnum.UNSTABLE.equals(checkedJobConfigData.getBuildStatus())) {
                build.setResult(Result.UNSTABLE);
                return true;
            }
        } catch (QGException e) {
            e.printStackTrace(listener.getLogger());
        }

        return buildHasPassed;
    }

    @Override
    public QGBuilderDescriptor getDescriptor() {
        return (QGBuilderDescriptor) super.getDescriptor();
    }
}
