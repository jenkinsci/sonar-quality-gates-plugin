package quality.gates.jenkins.plugin;

import hudson.EnvVars;
import hudson.Util;
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

        if (projectKey.startsWith("$")) {
            String systemVariableName = projectKey;
            String getEnvVariable = systemVariableName.substring(2, systemVariableName.length() - 1);
            projectKey = System.getenv(getEnvVariable);

            if (projectKey == null) {
                throw new QGException("Environment variable with name '" + getEnvVariable + "' does not exist.");
            }
        } else {
            try {
                projectKey = URLDecoder.decode(projectKey, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new QGException("Error while decoding the project key. UTF-8 not supported.", e);
            }
        }

        String name;

        if (!globalConfig.fetchListOfGlobalConfigData().isEmpty()) {
            name = hasFormDataKey(formData, globalConfig);
        } else {
            name = "";
        }

        firstInstanceJobConfigData.setProjectKey(projectKey);
        firstInstanceJobConfigData.setSonarInstanceName(name);

        return firstInstanceJobConfigData;
    }

    protected String hasFormDataKey(JSONObject formData, GlobalConfig globalConfig) {

        String instanceName;

        if (formData.containsKey("sonarInstancesName")) {
            instanceName = formData.getString("sonarInstancesName");
        } else {
            instanceName = globalConfig.fetchListOfGlobalConfigData().get(0).getName();
        }

        return instanceName;
    }

    public JobConfigData checkProjectKeyIfVariable(JobConfigData jobConfigData, AbstractBuild build, BuildListener listener) throws QGException {

        String projectKey = jobConfigData.getProjectKey();

        if (projectKey.isEmpty()) {
            throw new QGException("Empty project key.");
        }

        projectKey = Util.replaceMacro(projectKey, build.getBuildVariables());

        try {
            EnvVars env = build.getEnvironment(listener);
            projectKey = Util.replaceMacro(projectKey, env);
        } catch (IOException | InterruptedException e) {
            throw new QGException(e);
        }

        JobConfigData envVariableJobConfigData = new JobConfigData();
        envVariableJobConfigData.setProjectKey(projectKey);
        envVariableJobConfigData.setSonarInstanceName(jobConfigData.getSonarInstanceName());

        return envVariableJobConfigData;
    }
}
