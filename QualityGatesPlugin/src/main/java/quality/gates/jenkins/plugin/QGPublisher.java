package quality.gates.jenkins.plugin;

import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

public class QGPublisher extends Recorder {

    private JobConfigData jobConfigData;
    private BuildDecision buildDecision;
    private JobExecutionService jobExecutionService;
    private QGPublisherDescriptor publisherDescriptor;
    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @DataBoundConstructor
    public QGPublisher(JobConfigData jobConfigData) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = new BuildDecision();
        this.jobExecutionService = new JobExecutionService();
        this.publisherDescriptor = jobExecutionService.getPublisherDescriptor();
        this.globalConfigDataForSonarInstance = null;

    }

    public QGPublisher(JobConfigData jobConfigData, BuildDecision buildDecision, JobExecutionService jobExecutionService, QGPublisherDescriptor publisherDescriptor, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = buildDecision;
        this.jobExecutionService = jobExecutionService;
        this.publisherDescriptor = publisherDescriptor;
        this.globalConfigDataForSonarInstance = globalConfigDataForSonarInstance;
    }

    public JobConfigData getJobConfigData() {
        return jobConfigData;
    }

    @Override
    public QGPublisherDescriptor getDescriptor() {
        return (QGPublisherDescriptor) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        publisherDescriptor = jobExecutionService.getPublisherDescriptor();
        GlobalConfig globalConfig = publisherDescriptor.getGlobalConfig();
        globalConfigDataForSonarInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData.getSonarInstanceName());

        if(globalConfigDataForSonarInstance == null) {
            listener.error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, jobConfigData.getSonarInstanceName());
            return false;
        }
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace(listener.getLogger());
        }
        Result result = build.getResult();
        if (Result.SUCCESS != result) {
            listener.getLogger().println("Previous steps failed the build.\nResult is: " + result);
            return false;
        }
        boolean buildPassed;
        try {
            buildPassed = buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData);
            if("".equals(jobConfigData.getSonarInstanceName()))
                listener.getLogger().println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
            listener.getLogger().println("PostBuild-Step: Quality Gates plugin build passed: " + String.valueOf(buildPassed).toUpperCase());
            return buildPassed;
        } catch (QGException e) {
            e.printStackTrace(listener.getLogger());
        }
        return false;
    }
}