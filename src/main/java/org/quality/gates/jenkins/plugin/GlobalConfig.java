package org.quality.gates.jenkins.plugin;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;

@Extension
public class GlobalConfig extends GlobalConfiguration {

    private List<GlobalConfigDataForSonarInstance> listOfGlobalConfigData;

    private GlobalConfigurationService globalConfigurationService;

    public GlobalConfig() {

        load();
        globalConfigurationService = new GlobalConfigurationService();
    }


    public GlobalConfig(List<GlobalConfigDataForSonarInstance> listOfGlobalConfigData, GlobalConfigurationService globalConfigurationService) {

        this.listOfGlobalConfigData = listOfGlobalConfigData;
        this.globalConfigurationService = globalConfigurationService;
    }

    public List<GlobalConfigDataForSonarInstance> getListOfGlobalConfigData() {

        load();
        return listOfGlobalConfigData;
    }

    public List<GlobalConfigDataForSonarInstance> fetchListOfGlobalConfigData() {
        return listOfGlobalConfigData;
    }

    public void setGlobalConfigDataForSonarInstances(List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstances) {
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

    public GlobalConfigDataForSonarInstance getSonarInstanceByName(String name) {

        for (GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance : listOfGlobalConfigData) {
            if (name.equals(globalConfigDataForSonarInstance.getName())) {
                return globalConfigDataForSonarInstance;
            }
        }

        return null;
    }
}
