package quality.gates.jenkins.plugin;

import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

public class QGPublisher extends Recorder {

    private JobConfigData jobConfigData;
    private BuildDecision buildDecision;
    private JobExecutionService jobExecutionService;

    public JobConfigData getJobConfigData() {
        return jobConfigData;
    }

    @DataBoundConstructor
    public QGPublisher(JobConfigData jobConfigData) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = new BuildDecision();
        this.jobExecutionService = new JobExecutionService();

    }

    public QGPublisher(JobConfigData jobConfigData, BuildDecision buildDecision, JobExecutionService jobExecutionService) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = buildDecision;
        this.jobExecutionService = jobExecutionService;
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
        QGPublisherDescriptor buildDescriptor;
        try {
            buildDescriptor = jobExecutionService.getPublisherDescriptor();
            GlobalConfig globalConfig = buildDescriptor.getGlobalConfig();
            boolean hasGlobalConfigWithSameName = jobExecutionService.hasGlobalConfigDataWithSameName(jobConfigData, globalConfig);
            if(!hasGlobalConfigWithSameName && globalConfig.getListOfGlobalConfigData().size() > 0) {
                listener.error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, jobConfigData.getGlobalConfigDataForSonarInstance().getName());
                return false;
            }
        }
        catch (QGException e){
            e.printStackTrace(listener.getLogger());
        }
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        Result result = build.getResult();
        if (Result.SUCCESS != result) {
            listener.getLogger().println("Previous steps failed the build.\nResult is: " + result);
            return false;
        }
        boolean buildPassed;
        try {
            buildPassed = buildDecision.getStatus(jobConfigData);
            if(jobConfigData.getGlobalConfigDataForSonarInstance().getName().equals(""))
                listener.getLogger().println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
            listener.getLogger().println("PostBuild-Step: Quality Gates plugin build passed: " + String.valueOf(buildPassed).toUpperCase());
            return buildPassed;
        } catch (QGException e) {
            e.printStackTrace(listener.getLogger());
        }
        return false;
    }
}