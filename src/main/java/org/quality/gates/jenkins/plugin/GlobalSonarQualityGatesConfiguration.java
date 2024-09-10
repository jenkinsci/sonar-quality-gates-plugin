package org.quality.gates.jenkins.plugin;

import hudson.Extension;
import hudson.util.FormValidation;
import java.util.List;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Symbol("sonarQualityGates")
public class GlobalSonarQualityGatesConfiguration extends GlobalConfiguration {

    private List<SonarInstance> listOfGlobalConfigData;

    private final GlobalConfigurationService globalConfigurationService;

    public GlobalSonarQualityGatesConfiguration() {
        load();
        globalConfigurationService = new GlobalConfigurationService();
    }

    public GlobalSonarQualityGatesConfiguration(
            List<SonarInstance> listOfGlobalConfigData, GlobalConfigurationService globalConfigurationService) {
        this.listOfGlobalConfigData = listOfGlobalConfigData;
        this.globalConfigurationService = globalConfigurationService;
    }

    public List<SonarInstance> getListOfGlobalConfigData() {
        load();
        return listOfGlobalConfigData;
    }

    public List<SonarInstance> fetchListOfGlobalConfigData() {
        return listOfGlobalConfigData;
    }

    @DataBoundSetter
    public void setGlobalConfigDataForSonarInstances(List<SonarInstance> globalConfigDataForSonarInstances) {
        this.listOfGlobalConfigData = globalConfigDataForSonarInstances;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        listOfGlobalConfigData = globalConfigurationService.instantiateGlobalConfigData(json);
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
        for (SonarInstance globalConfigDataForSonarInstance : listOfGlobalConfigData) {
            if (name.equals(globalConfigDataForSonarInstance.getName())) {
                return globalConfigDataForSonarInstance;
            }
        }

        return null;
    }
}
