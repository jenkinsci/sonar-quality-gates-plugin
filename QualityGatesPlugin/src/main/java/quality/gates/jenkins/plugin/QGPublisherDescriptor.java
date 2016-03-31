package quality.gates.jenkins.plugin;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import javax.inject.Inject;

@Extension
public final class QGPublisherDescriptor extends BuildStepDescriptor<Publisher> {

    @Inject
    private GlobalConfig globalConfig;
    @Inject
    private JobConfigurationService jobConfigurationService;

    public QGPublisherDescriptor() {
        super(QGPublisher.class);
        load();
    }

    public QGPublisherDescriptor(GlobalConfig globalConfig, JobConfigurationService jobConfigurationService) {
        super(QGPublisher.class);
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
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Quality Gates";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        save();
        return true;
    }

    @Override
    public QGPublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException,QGException {
        JobConfigData firstInstanceJobConfigData = jobConfigurationService.createJobConfigData(formData, globalConfig);
        return new QGPublisher(firstInstanceJobConfigData);
    }
}
