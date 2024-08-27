package org.quality.gates.jenkins.plugin;

import hudson.Util;
import hudson.util.Secret;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

public class GlobalConfigurationService {

    private List<GlobalConfigDataForSonarInstance> listOfGlobalConfigInstances;

    public void setListOfGlobalConfigInstances(List<GlobalConfigDataForSonarInstance> listOfGlobalConfigInstances) {
        this.listOfGlobalConfigInstances = listOfGlobalConfigInstances;
    }

    protected List<GlobalConfigDataForSonarInstance> instantiateGlobalConfigData(JSONObject json) {
        listOfGlobalConfigInstances = new ArrayList<>();
        var globalDataConfigs = (JSON) json.opt("listOfGlobalConfigData");

        if (globalDataConfigs == null) {
            globalDataConfigs = new JSONArray();
        }

        initGlobalDataConfig(globalDataConfigs);

        return listOfGlobalConfigInstances;
    }

    protected void initGlobalDataConfig(JSON globalDataConfigs) {
        var array = getGlobalConfigsArray(globalDataConfigs);

        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonobject = array.getJSONObject(i);
            addGlobalConfigDataForSonarInstance(jsonobject);
        }
    }

    protected JSONArray getGlobalConfigsArray(JSON globalDataConfigs) {
        if (globalDataConfigs.isArray()) {
            return JSONArray.class.cast(globalDataConfigs);
        }

        return createJsonArrayFromObject((JSONObject) globalDataConfigs);
    }

    protected JSONArray createJsonArrayFromObject(JSONObject globalDataConfigs) {
        var array = new JSONArray();
        array.add(globalDataConfigs);

        return array;
    }

    protected void addGlobalConfigDataForSonarInstance(JSONObject globalConfigData) {
        var name = globalConfigData.optString("name");
        var timeToWait = globalConfigData.optInt("timeToWait");
        var maxWaitTime = globalConfigData.optInt("maxWaitTime");
        var url = globalConfigData.optString("url");

        if (!"".equals(name)) {
            GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;
            var token = globalConfigData.optString("token");
            if (StringUtils.isNotEmpty(token)) {
                globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance(
                        name,
                        url,
                        Secret.fromString(Util.fixEmptyAndTrim(globalConfigData.optString("token"))),
                        timeToWait,
                        maxWaitTime);
            } else {
                globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance(
                        name,
                        url,
                        globalConfigData.optString("account"),
                        Secret.fromString(Util.fixEmptyAndTrim(globalConfigData.optString("password"))),
                        timeToWait,
                        maxWaitTime);
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
