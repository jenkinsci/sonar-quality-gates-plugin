package org.quality.gates.jenkins.plugin;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.Arrays;
import javax.inject.Inject;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.quality.gates.jenkins.plugin.enumeration.BuildStatusEnum;

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

    public QGBuilderDescriptor(
            JobExecutionService jobExecutionService, JobConfigurationService jobConfigurationService) {
        super(QGBuilder.class);

        this.jobConfigurationService = jobConfigurationService;
        this.jobExecutionService = jobExecutionService;
    }

    public ListBoxModel doFillListOfGlobalConfigDataItems() {
        return jobConfigurationService.getListOfSonarInstanceNames(jobExecutionService.getGlobalConfigData());
    }

    public ListBoxModel doFillBuildStatusItems() {
        var items = new ListBoxModel();
        Arrays.asList(BuildStatusEnum.values()).forEach(e -> items.add(e.toString()));
        return items;
    }

    public FormValidation doCheckProjectKey(@QueryParameter String projectKey) {
        if (projectKey.isEmpty()) {
            return FormValidation.error("Please insert project key.");
        }

        return FormValidation.ok();
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Quality Gates Sonarqube Plugin";
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
        var firstInstanceJobConfigData =
                jobConfigurationService.createJobConfigData(formData, jobExecutionService.getGlobalConfigData());
        var globalConfigDataForSonarInstance = jobExecutionService
                .getGlobalConfigData()
                .getSonarInstanceByName(firstInstanceJobConfigData.getSonarInstanceName());

        return new QGBuilder(firstInstanceJobConfigData, globalConfigDataForSonarInstance);
    }
}
