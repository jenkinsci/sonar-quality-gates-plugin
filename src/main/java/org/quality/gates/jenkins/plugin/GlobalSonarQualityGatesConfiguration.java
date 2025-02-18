package org.quality.gates.jenkins.plugin;

import hudson.Extension;
import hudson.util.FormValidation;
import java.util.List;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

@Extension
@Symbol("sonarQualityGates")
public class GlobalSonarQualityGatesConfiguration extends GlobalConfiguration {

    private List<SonarInstance> sonarInstances;

    private final GlobalConfigurationService globalConfigurationService;

    public GlobalSonarQualityGatesConfiguration() {
        load();
        globalConfigurationService = new GlobalConfigurationService();
    }

    public GlobalSonarQualityGatesConfiguration(
            List<SonarInstance> sonarInstances, GlobalConfigurationService globalConfigurationService) {
        this.sonarInstances = sonarInstances;
        this.globalConfigurationService = globalConfigurationService;
    }

    public List<SonarInstance> getSonarInstances() {
        load();
        return sonarInstances;
    }

    public List<SonarInstance> fetchSonarInstances() {
        return sonarInstances;
    }

    @DataBoundSetter
    public void setSonarInstances(List<SonarInstance> globalConfigDataForSonarInstances) {
        this.sonarInstances = globalConfigDataForSonarInstances;
    }

    @Override
    public boolean configure(StaplerRequest2 req, JSONObject json) throws FormException {
        sonarInstances = globalConfigurationService.instantiateGlobalConfigData(json);
        save();

        return true;
    }

    public FormValidation doCheckName(@QueryParameter String name) {
        if (name.isEmpty()) {
            return FormValidation.error("Please insert a name for the instance.");
        }

        return FormValidation.ok();
    }

    public SonarInstance getSonarInstanceByName(String name) {
        for (SonarInstance sonarInstance : sonarInstances) {
            if (name.equals(sonarInstance.getName())) {
                return sonarInstance;
            }
        }

        return null;
    }
}
