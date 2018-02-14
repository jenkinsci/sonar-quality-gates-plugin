package org.quality.gates.jenkins.plugin;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.quality.gates.jenkins.plugin.enumeration.BuildStatusEnum;

import javax.inject.Inject;
import java.util.Arrays;

@Extension
public final class QGPublisherDescriptor extends BuildStepDescriptor<Publisher> {

    @Inject
    private JobConfigurationService jobConfigurationService;

    @Inject
    private JobExecutionService jobExecutionService;

    public QGPublisherDescriptor() {

        super(QGPublisher.class);
        load();
    }

    public QGPublisherDescriptor(JobExecutionService jobExecutionService, JobConfigurationService jobConfigurationService) {

        super(QGPublisher.class);
        this.jobExecutionService = jobExecutionService;
        this.jobConfigurationService = jobConfigurationService;
    }

    public JobExecutionService getJobExecutionService() {
        return jobExecutionService;
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

    public ListBoxModel doFillBuildStatusItems() {
        ListBoxModel items = new ListBoxModel();
        Arrays.asList(BuildStatusEnum.values()).forEach(e -> items.add(e.toString()));
        return items;
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Quality Gates Sonarqube Plugin";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {

        save();
        return true;
    }

    @Override
    public QGPublisher newInstance(StaplerRequest req, JSONObject formData) throws QGException {

        JobConfigData firstInstanceJobConfigData = jobConfigurationService.createJobConfigData(formData, jobExecutionService.getGlobalConfigData());
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = jobExecutionService.getGlobalConfigData().getSonarInstanceByName(firstInstanceJobConfigData.getSonarInstanceName());
        return new QGPublisher(firstInstanceJobConfigData, globalConfigDataForSonarInstance);
    }
}
