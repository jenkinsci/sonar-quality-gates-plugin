package quality.gates.jenkins.plugin;

import hudson.util.ListBoxModel;
import hudson.util.Secret;
import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class JobConfigurationService {

    public ListBoxModel getListOfSonarInstanceNames(GlobalConfig globalConfig) {
        ListBoxModel listBoxModel = new ListBoxModel();
        for (GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance : globalConfig.fetchListOfGlobalConfigData()) {
            listBoxModel.add(globalConfigDataForSonarInstance.getName());
        }
        return listBoxModel;
    }

    public JobConfigData createJobConfigData(JSONObject formData, GlobalConfig globalConfig) {
        JobConfigData firstInstanceJobConfigData = new JobConfigData();
        String projectKey = formData.getString("projectKey");
        String name;
        try {
            projectKey = URLDecoder.decode(projectKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new QGException("Error while decoding the project key. UTF-8 not supported.", e);
        }
        if(!globalConfig.fetchListOfGlobalConfigData().isEmpty()) {
            name = hasFormDataKey(formData, globalConfig);
        }
        else {
            name = "";
        }
        firstInstanceJobConfigData.setProjectKey(projectKey);
        firstInstanceJobConfigData.setSonarInstanceName(name);
        return firstInstanceJobConfigData;
    }

    protected String hasFormDataKey(JSONObject formData, GlobalConfig globalConfig) {
        String instanceName;
        if (formData.containsKey("sonarInstancesName"))
            instanceName = formData.getString("sonarInstancesName");
        else
            instanceName = globalConfig.fetchListOfGlobalConfigData().get(0).getName();
        return instanceName;
    }
}
