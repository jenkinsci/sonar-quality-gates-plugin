package org.quality.gates.jenkins.plugin;

import static com.cloudbees.plugins.credentials.CredentialsProvider.listCredentialsInItem;

import com.cloudbees.plugins.credentials.common.*;
import hudson.Extension;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.List;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class GlobalConfig extends GlobalConfiguration {

    private final GlobalConfigurationService globalConfigurationService;

    private List<GlobalConfigDataForSonarInstance> listOfGlobalConfigData;

    public GlobalConfig() {
        load();
        globalConfigurationService = new GlobalConfigurationService();
    }

    public GlobalConfig(
            List<GlobalConfigDataForSonarInstance> listOfGlobalConfigData,
            GlobalConfigurationService globalConfigurationService) {
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

    public void setGlobalConfigDataForSonarInstances(
            List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstances) {
        this.listOfGlobalConfigData = globalConfigDataForSonarInstances;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        listOfGlobalConfigData = globalConfigurationService.instantiateGlobalConfigData(json);
        save();
        return true;
    }

    public ListBoxModel doFillSonarCredentialsIdItems(
            @AncestorInPath Item item, @QueryParameter String sonarCredentials) {
        var result = new StandardListBoxModel();

        var credentials = listCredentialsInItem(StringCredentialsImpl.class, null, null, null, null);
        credentials.forEach(c -> result.add(c.value));

        return result;
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
