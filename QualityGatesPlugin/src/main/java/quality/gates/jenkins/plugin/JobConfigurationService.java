package quality.gates.jenkins.plugin;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;

import java.io.IOException;
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
        if(projectKey.startsWith("$"))
        {
            String systemVariableName = projectKey;
            String getEnvVariable = systemVariableName.substring(2, systemVariableName.length()-1);
            projectKey = System.getenv(getEnvVariable);
            if(projectKey == null) {
                throw new QGException("Environment variable with name '" + getEnvVariable + "' does not exist.");
            }
        }
        else {
            try {
                projectKey = URLDecoder.decode(projectKey, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new QGException("Error while decoding the project key. UTF-8 not supported.", e);
            }
        }
        String name;

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

    public JobConfigData checkProjectKeyIfVariable(JobConfigData jobConfigData, AbstractBuild build, BuildListener listener) throws QGException {
        JobConfigData envVariableJobConfigData = new JobConfigData();
        envVariableJobConfigData.setProjectKey(jobConfigData.getProjectKey());
        envVariableJobConfigData.setSonarInstanceName(jobConfigData.getSonarInstanceName());
        if(jobConfigData.getProjectKey().isEmpty()) {
            throw new QGException("Empty project key.");
        }
        if(jobConfigData.getProjectKey().startsWith("$")) {
            String stripProjectKey = jobConfigData.getProjectKey().substring(1);
            if(stripProjectKey.startsWith("{") && stripProjectKey.endsWith("}")) {
                stripProjectKey = stripProjectKey.substring(1, stripProjectKey.length()-1);
            }
            try {
                String getEnvVariable = build.getEnvironment(listener).get(stripProjectKey);
                if(getEnvVariable != null) {
                    envVariableJobConfigData.setProjectKey(getEnvVariable);
                }
                else {
                    throw new QGException("Environment variable with name '" + stripProjectKey + "' was not found.");
                }
            } catch (IOException e) {
                throw new QGException(e);
            } catch (InterruptedException e) {
                throw new QGException(e);
            }
        }
        return envVariableJobConfigData;
    }
}
