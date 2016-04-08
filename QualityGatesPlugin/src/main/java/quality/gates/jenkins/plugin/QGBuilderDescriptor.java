package quality.gates.jenkins.plugin;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import javax.inject.Inject;

@Extension
public final class QGBuilderDescriptor extends BuildStepDescriptor<Builder> {


    @Inject
    private GlobalConfig globalConfig;

    @Inject
    private JobConfigurationService jobConfigurationService;

    public QGBuilderDescriptor()
    {
        super(QGBuilder.class);
        load();
    }

    public QGBuilderDescriptor(GlobalConfig globalConfig, JobConfigurationService jobConfigurationService){
        super(QGBuilder.class);
        this.globalConfig = globalConfig;
        this.jobConfigurationService = jobConfigurationService;
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public ListBoxModel doFillListOfGlobalConfigDataItems() {
        return jobConfigurationService.getListOfSonarInstanceNames(globalConfig);
    }

    @Override
    public String getDisplayName() {
        return "Quality Gates";
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Override
    public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
        save();
        return true;
    }

    @Override
    public QGBuilder newInstance(StaplerRequest req, JSONObject formData) throws QGException {
        JobConfigData firstInstanceJobConfigData = jobConfigurationService.createJobConfigData(formData, globalConfig);
        return new QGBuilder(firstInstanceJobConfigData);
    }
}
