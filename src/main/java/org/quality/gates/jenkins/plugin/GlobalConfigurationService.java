package org.quality.gates.jenkins.plugin;

import hudson.Util;
import hudson.util.Secret;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GlobalConfigurationService {

    private List<GlobalConfigDataForSonarInstance> listOfGlobalConfigInstances;

    public void setListOfGlobalConfigInstances(List<GlobalConfigDataForSonarInstance> listOfGlobalConfigInstances) {
        this.listOfGlobalConfigInstances = listOfGlobalConfigInstances;
    }

    protected List<GlobalConfigDataForSonarInstance> instantiateGlobalConfigData(JSONObject json) {

        listOfGlobalConfigInstances = new ArrayList<>();
        JSON globalDataConfigs = (JSON) json.opt("listOfGlobalConfigData");

        if (globalDataConfigs == null) {
            globalDataConfigs = new JSONArray();
        }

        initGlobalDataConfig(globalDataConfigs);

        return listOfGlobalConfigInstances;
    }

    protected void initGlobalDataConfig(JSON globalDataConfigs) {

        JSONArray array = getGlobalConfigsArray(globalDataConfigs);

        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonobject = array.getJSONObject(i);
            addGlobalConfigDataForSonarInstance(jsonobject);
        }
    }

    protected JSONArray getGlobalConfigsArray(JSON globalDataConfigs) {

        JSONArray array;

        if (globalDataConfigs.isArray()) {
            array = JSONArray.class.cast(globalDataConfigs);
        } else {
            array = createJsonArrayFromObject((JSONObject) globalDataConfigs);
        }

        return array;
    }

    protected JSONArray createJsonArrayFromObject(JSONObject globalDataConfigs) {

        JSONArray array = new JSONArray();
        array.add(globalDataConfigs);

        return array;
    }

    protected void addGlobalConfigDataForSonarInstance(JSONObject globalConfigData) {

        String name = globalConfigData.optString("name");
        int timeToWait = globalConfigData.optInt("timeToWait");
		int maxWaitTime = globalConfigData.optInt("maxWaitTime");
        String url = globalConfigData.optString("url");

        if (!"".equals(name)) {

            GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;
            String token = globalConfigData.optString("token");
            if (StringUtils.isNotEmpty(token)) {
                globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance(name, url, globalConfigData.optString("token"), timeToWait, maxWaitTime);
            } else {
                globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance(name, url, globalConfigData.optString("account"), Secret.fromString(Util.fixEmptyAndTrim(globalConfigData.optString("password"))), timeToWait, maxWaitTime);
            }

            if (!containsGlobalConfigWithName(name)) {
                listOfGlobalConfigInstances.add(globalConfigDataForSonarInstance);
            }
        }
    }

    protected boolean containsGlobalConfigWithName(String name) {

        for (GlobalConfigDataForSonarInstance globalConfigDataInstance : listOfGlobalConfigInstances) {
            if (globalConfigDataInstance.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }
}
