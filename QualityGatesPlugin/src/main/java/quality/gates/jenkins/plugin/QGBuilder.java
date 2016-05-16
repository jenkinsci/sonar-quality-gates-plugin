package quality.gates.jenkins.plugin;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;


public class QGBuilder extends Builder {

    private JobConfigData jobConfigData;
    private BuildDecision buildDecision;
    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;
    private JobExecutionService jobExecutionService;

    @DataBoundConstructor
    public QGBuilder(JobConfigData jobConfigData) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = new BuildDecision();
        this.jobExecutionService = new JobExecutionService();
        this.globalConfigDataForSonarInstance = null;
    }

    protected QGBuilder(JobConfigData jobConfigData, BuildDecision buildDecision, JobExecutionService jobExecutionService, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = buildDecision;
        this.jobExecutionService = jobExecutionService;
        this.globalConfigDataForSonarInstance = globalConfigDataForSonarInstance;
    }

    public JobConfigData getJobConfigData() {
        return jobConfigData;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        globalConfigDataForSonarInstance = buildDecision.chooseSonarInstance(jobExecutionService.getGlobalConfigData(), jobConfigData);
        if(globalConfigDataForSonarInstance == null) {
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
        boolean buildHasPassed;
        try {
            buildHasPassed = buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData);
            if("".equals(jobConfigData.getSonarInstanceName()))
                listener.getLogger().println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
            listener.getLogger().println("Build-Step: Quality Gates plugin build passed: " + String.valueOf(buildHasPassed).toUpperCase());
            return buildHasPassed;
        }
        catch (QGException e){
            e.printStackTrace(listener.getLogger());
        }
        return false;
    }

    @Override
    public QGBuilderDescriptor getDescriptor() {
        return (QGBuilderDescriptor) super.getDescriptor();
    }


}
