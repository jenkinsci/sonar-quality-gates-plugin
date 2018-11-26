package org.quality.gates.jenkins.plugin;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.quality.gates.jenkins.plugin.enumeration.BuildStatusEnum;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobConfigurationService {

    private static final Pattern ENV_VARIABLE_WITH_BRACES_PATTERN = Pattern.compile("(\\$\\{[a-zA-Z0-9_]+\\})");

    private static final Pattern ENV_VARIABLE_WITHOUT_BRACES_PATTERN = Pattern.compile("(\\$[a-zA-Z0-9_]+)");

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

        if (!projectKey.startsWith("$")) {
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
        firstInstanceJobConfigData.setBuildStatus(BuildStatusEnum.valueOf(formData.getString("buildStatus")));

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

        final JobConfigData envVariableJobConfigData = new JobConfigData();
        envVariableJobConfigData.setSonarInstanceName(jobConfigData.getSonarInstanceName());

        try {
            envVariableJobConfigData.setProjectKey(getProjectKey(projectKey, build.getEnvironment(listener)));
        } catch (IOException | InterruptedException e) {
            throw new QGException(e);
        }

        envVariableJobConfigData.setSonarInstanceName(jobConfigData.getSonarInstanceName());
        envVariableJobConfigData.setBuildStatus(jobConfigData.getBuildStatus());

        return envVariableJobConfigData;
    }

    private String getProjectKey(final String projectKey, EnvVars env) {

        final String projectKeyAfterFirstResolving = resolveEmbeddedEnvVariables(projectKey, env, ENV_VARIABLE_WITH_BRACES_PATTERN, 1);

        return resolveEmbeddedEnvVariables(projectKeyAfterFirstResolving, env, ENV_VARIABLE_WITHOUT_BRACES_PATTERN, 0);
    }

    private String resolveEmbeddedEnvVariables(final String projectKey, final EnvVars env, final Pattern pattern, final int braceOffset) {

        final Matcher matcher = pattern.matcher(projectKey);
        final StringBuilder builder = new StringBuilder(projectKey);
        boolean matchesFound = false;
        int offset = 0;

        while (matcher.find()) {
            final String envVariable = projectKey.substring(matcher.start() + braceOffset + 1, matcher.end() - braceOffset);
            final String envValue = env.get(envVariable);

            if (envValue == null) {
                throw new QGException("Environment Variable [" + envVariable + "] not found");
            }

            builder.replace(matcher.start() + offset, matcher.end() + offset, envValue);
            offset += envValue.length() - matcher.group(1).length();
            matchesFound = true;
        }

        if (matchesFound) {
            return getProjectKey(builder.toString(), env);
        }

        return builder.toString();
    }
}
