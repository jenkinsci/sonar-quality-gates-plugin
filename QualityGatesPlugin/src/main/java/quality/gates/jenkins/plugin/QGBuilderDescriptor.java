package quality.gates.jenkins.plugin;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.inject.Inject;

@Extension
public final class QGBuilderDescriptor extends BuildStepDescriptor<Builder> {

    @Inject
    private JobConfigurationService jobConfigurationService;

    @Inject
    private JobExecutionService jobExecutionService;

    public QGBuilderDescriptor() {

        super(QGBuilder.class);
        load();
    }

    public QGBuilderDescriptor(JobExecutionService jobExecutionService, JobConfigurationService jobConfigurationService) {

        super(QGBuilder.class);

        this.jobConfigurationService = jobConfigurationService;
        this.jobExecutionService = jobExecutionService;
    }

    public ListBoxModel doFillListOfGlobalConfigDataItems() {
        return jobConfigurationService.getListOfSonarInstanceNames(jobExecutionService.getGlobalConfigData());
    }

    public FormValidation doCheckProjectKey(@QueryParameter String projectKey) {

        if (projectKey.isEmpty()) {
            return FormValidation.error("Please insert project key.");
        }

        return FormValidation.ok();
    }

    @Override
    public String getDisplayName() {
        return "Quality Gates - Sonarqube 6.x+";
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

        JobConfigData firstInstanceJobConfigData = jobConfigurationService.createJobConfigData(formData, jobExecutionService.getGlobalConfigData());
        return new QGBuilder(firstInstanceJobConfigData);
    }
}
