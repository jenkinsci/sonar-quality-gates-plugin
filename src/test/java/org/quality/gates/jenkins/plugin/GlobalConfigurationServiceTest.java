package org.quality.gates.jenkins.plugin;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.jenkins.plugin.GlobalConfigurationService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class GlobalConfigurationServiceTest {

    private GlobalConfigurationService globalConfigurationService;

    private GlobalConfigurationService spyGlobalConfigurationService;

    @Mock
    private List<GlobalConfigDataForSonarInstance> listOfGlobalConfigData;

    private JSONObject jsonObjectNotNull;

    private JSON globalDataConfigs;

    private JSONArray jsonArray;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        globalConfigurationService = new GlobalConfigurationService();
        spyGlobalConfigurationService = spy(globalConfigurationService);
        listOfGlobalConfigData = new ArrayList<>();
        jsonObjectNotNull = new JSONObject();
        jsonObjectNotNull.put("name", "Sonar");
        jsonObjectNotNull.put("url", "http://localhost:9000");
        jsonObjectNotNull.put("account", "admin");
        jsonObjectNotNull.put("pass", "admin");
    }

    @Test
    public void testGetGlobalConfigsArrayWhenObject() {
        jsonArray = new JSONArray();
        doReturn(jsonArray).when(spyGlobalConfigurationService).createJsonArrayFromObject(JSONObject.fromObject(globalDataConfigs));
        String objectString = "{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}";
        globalDataConfigs = JSONSerializer.toJSON(objectString);
        jsonArray.add(globalDataConfigs);
        assertEquals(jsonArray, spyGlobalConfigurationService.getGlobalConfigsArray(globalDataConfigs));
    }

    @Test
    public void testGetGlobalConfigsArrayWhenArray() {
        String arrayString = "[{\"name\":\"Sonar1\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"},{\"name\":\"Sonar2\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}]";
        globalDataConfigs = JSONSerializer.toJSON(arrayString);
        jsonArray = JSONArray.class.cast(globalDataConfigs);
        assertEquals(jsonArray, globalConfigurationService.getGlobalConfigsArray(globalDataConfigs));
    }

    @Test
    public void testCreateJsonArrayFromObject() {
        String array = "[{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"pass\":\"admin\"}]";
        assertEquals(JSONArray.fromObject(array), globalConfigurationService.createJsonArrayFromObject(jsonObjectNotNull));
    }

    @Test
    public void testInstantiateGlobalConfigData() {
        JSONObject json = new JSONObject();
        json.put("listOfGlobalConfigData", JSONArray.fromObject("[{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}]"));
        JSON globalDataConfig = (JSON) json.opt("listOfGlobalConfigData");
        doNothing().when(spyGlobalConfigurationService).initGlobalDataConfig(globalDataConfig);
        assertEquals(listOfGlobalConfigData, spyGlobalConfigurationService.instantiateGlobalConfigData(json));
    }

    @Test
    public void testInstantiateGlobalConfigDataWhenJsonIsNull() {
        JSONObject json = new JSONObject();
        doNothing().when(spyGlobalConfigurationService).initGlobalDataConfig(any(JSON.class));
        assertEquals(listOfGlobalConfigData, spyGlobalConfigurationService.instantiateGlobalConfigData(json));
    }

    @Test
    public void testContainsGlobalConfigWithNameTrue() {
        String name = "Ime";
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance();
        globalConfigDataForSonarInstance.setName("Ime");
        listOfGlobalConfigData.add(globalConfigDataForSonarInstance);
        spyGlobalConfigurationService.setListOfGlobalConfigInstances(listOfGlobalConfigData);
        assertTrue(spyGlobalConfigurationService.containsGlobalConfigWithName(name));
    }

    @Test
    public void testContainsGlobalConfigWithNameFalse() {
        String name = "Ime";
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance();
        globalConfigDataForSonarInstance.setName("Ime3");
        listOfGlobalConfigData.add(globalConfigDataForSonarInstance);
        spyGlobalConfigurationService.setListOfGlobalConfigInstances(listOfGlobalConfigData);
        assertFalse(spyGlobalConfigurationService.containsGlobalConfigWithName(name));
    }
}
