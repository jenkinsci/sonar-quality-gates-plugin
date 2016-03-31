package quality.gates.jenkins.plugin;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GlobalConfigurationService {

    public static final Logger LOGGER = LoggerFactory.getLogger(QGBuilderDescriptor.class);

    private List<GlobalConfigDataForSonarInstance> listOfGlobalConfigInstances;

    public GlobalConfigurationService() {
        this.listOfGlobalConfigInstances = new ArrayList<>();
    }

    protected List<GlobalConfigDataForSonarInstance> instantiateGlobalConfigData(JSONObject json) {
        LOGGER.info(String.format("JSON in the GlobalConfig: %s", json));

        if (isNotNullJson(json)) {
            setGlobalDataConfigWhenNotNull(json);
            LOGGER.info(String.format("JSON in the GlobalConfig after: %s", json));
        }
        return listOfGlobalConfigInstances;
    }

    protected boolean isNotNullJson(JSONObject json) {
        return json != null && !json.isNullObject();
    }

    protected boolean isNotEmptyOrNull(JSON globalDataConfigs) {
        return globalDataConfigs != null && !globalDataConfigs.isEmpty();
    }

    protected void setGlobalDataConfigWhenNotNull(JSONObject json) {
        JSON globalDataConfigs = (JSON) json.opt("listOfGlobalConfigData");
        if (isNotEmptyOrNull(globalDataConfigs)) {
            initGlobalDataConfig(globalDataConfigs);
        }
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
        if (!"".equals(name)) {
            GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance(name,
                    globalConfigData.optString("url"), globalConfigData.optString("account"), globalConfigData.optString("password"));
            if (!listOfGlobalConfigInstances.contains(globalConfigDataForSonarInstance)) {
                listOfGlobalConfigInstances.add(globalConfigDataForSonarInstance);
            }
        }
    }

}
