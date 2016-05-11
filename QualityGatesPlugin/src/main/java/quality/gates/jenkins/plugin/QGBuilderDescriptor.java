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

    public FormValidation doCheckProjectKey(@QueryParameter String projectKey) {
        boolean isEnvVar = false;
        if(projectKey.isEmpty())
            return FormValidation.error("Please insert project key.");

        if(projectKey.startsWith("$")) {
            if(projectKey.length() > 1) {
                if(projectKey.charAt(1) == '{') {
                    if(projectKey.endsWith("}")) {
                        System.out.println("CHECKS FOR ENV VARIABLE");
                        String unstrippedEnvVariable = projectKey;
                        System.out.println("Unstripped Variable = " + unstrippedEnvVariable);
                        String strippedEnvVariable = unstrippedEnvVariable.substring(2, unstrippedEnvVariable.length()-1);
                        System.out.println("Stripped Variable" + strippedEnvVariable);
                        String envVariable = System.getenv(strippedEnvVariable);
                        System.out.println("WHEN GET VARIABLE IS CALLED = " + envVariable);
                        if(envVariable == null) {
                            System.out.println("ENV VARIABLE IS NULL");
                            FormValidation.error("Environment variable with name '" + strippedEnvVariable + "' does not exist.");
                        }
                        return FormValidation.ok();
                    } else {
                        return FormValidation.error("Environment variable must finish with a '}'");
                    }
                }
            }
            else {
                return FormValidation.error("Environment variable must start with '${'");
            }
        }
        return FormValidation.ok();
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
