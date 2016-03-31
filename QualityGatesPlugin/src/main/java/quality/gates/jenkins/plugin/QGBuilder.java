package quality.gates.jenkins.plugin;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QGBuilder extends Builder {

    private static final Logger LOGGER = LoggerFactory.getLogger(QGBuilder.class);

    private JobConfigData jobConfigData;
    private BuildDecision buildDecision;
    private JobExecutionService jobExecutionService;

    public JobConfigData getJobConfigData() {
        return jobConfigData;
    }

    @DataBoundConstructor
    public QGBuilder(JobConfigData jobConfigData) {
        this.jobConfigData = jobConfigData;
        this.jobExecutionService = new JobExecutionService();
        this.buildDecision = new BuildDecision();
    }

    protected QGBuilder(JobConfigData jobConfigData, BuildDecision buildDecision, JobExecutionService jobExecutionService) {
        this.jobConfigData = jobConfigData;
        this.buildDecision = buildDecision;
        this.jobExecutionService = jobExecutionService;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        QGBuilderDescriptor buildDescriptor;
        try {
            buildDescriptor = jobExecutionService.getBuilderDescriptor();
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
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        boolean buildPassed;
        try {
            buildPassed = buildDecision.getStatus(jobConfigData);
            if(jobConfigData.getGlobalConfigDataForSonarInstance().getName().equals(""))
                listener.getLogger().println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
            listener.getLogger().println("Build-Step: Quality Gates plugin build passed: " + String.valueOf(buildPassed).toUpperCase());
            return buildPassed;
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
