package quality.gates.jenkins.plugin;

import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class JobConfigurationService {

    public ListBoxModel getListOfSonarInstanceNames(GlobalConfig globalConfig) {
        ListBoxModel listBoxModel = new ListBoxModel();
        for (GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance : globalConfig.getListOfGlobalConfigData()) {
            listBoxModel.add(globalConfigDataForSonarInstance.getName());
        }
        return listBoxModel;
    }

    public JobConfigData createJobConfigData(JSONObject formData, GlobalConfig globalConfig) {
        JobConfigData firstInstanceJobConfigData = new JobConfigData();
        String projectKey = formData.getString("projectKey");
        try {
            projectKey = URLDecoder.decode(projectKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new QGException("Error while decoding the project key. UTF-8 not supported.", e);
        }
        firstInstanceJobConfigData.setProjectKey(projectKey);
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;
        if(!globalConfig.getListOfGlobalConfigData().isEmpty()) {
            globalConfigDataForSonarInstance = hasFormDataKey(formData, globalConfig);
        }
        else
            globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance("", "http://localhost:9000", "admin", "admin");
        firstInstanceJobConfigData.setGlobalConfigDataForSonarInstance(globalConfigDataForSonarInstance);
        return firstInstanceJobConfigData;
    }

    protected GlobalConfigDataForSonarInstance hasFormDataKey(JSONObject formData, GlobalConfig globalConfig) {
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;
        if (formData.containsKey("sonarInstancesNames"))
            globalConfigDataForSonarInstance = globalConfig.getSonarInstanceByName(formData.getString("sonarInstancesNames"));
        else
            globalConfigDataForSonarInstance = globalConfig.getListOfGlobalConfigData().get(0);
        return globalConfigDataForSonarInstance;
    }
}
