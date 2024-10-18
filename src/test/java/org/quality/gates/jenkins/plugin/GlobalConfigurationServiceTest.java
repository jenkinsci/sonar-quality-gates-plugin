package org.quality.gates.jenkins.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GlobalConfigurationServiceTest {

    private GlobalConfigurationService globalConfigurationService;

    private GlobalConfigurationService spyGlobalConfigurationService;

    @Mock
    private List<SonarInstance> sonarInstances;

    private JSONObject jsonObjectNotNull;

    private JSON globalDataConfigs;

    private JSONArray jsonArray;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        globalConfigurationService = new GlobalConfigurationService();
        spyGlobalConfigurationService = spy(globalConfigurationService);
        sonarInstances = new ArrayList<>();
        jsonObjectNotNull = new JSONObject();
        jsonObjectNotNull.put("name", "Sonar");
        jsonObjectNotNull.put("url", "http://localhost:9000");
        jsonObjectNotNull.put("account", "admin");
        jsonObjectNotNull.put("pass", "admin");
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetGlobalConfigsArrayWhenObject() {
        jsonArray = new JSONArray();
        doReturn(jsonArray)
                .when(spyGlobalConfigurationService)
                .createJsonArrayFromObject(JSONObject.fromObject(globalDataConfigs));
        String objectString =
                "{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}";
        globalDataConfigs = JSONSerializer.toJSON(objectString);
        jsonArray.add(globalDataConfigs);
        assertEquals(jsonArray, spyGlobalConfigurationService.getGlobalConfigsArray(globalDataConfigs));
    }

    @Test
    public void testGetGlobalConfigsArrayWhenArray() {
        String arrayString =
                "[{\"name\":\"Sonar1\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"},{\"name\":\"Sonar2\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}]";
        globalDataConfigs = JSONSerializer.toJSON(arrayString);
        jsonArray = JSONArray.class.cast(globalDataConfigs);
        assertEquals(jsonArray, globalConfigurationService.getGlobalConfigsArray(globalDataConfigs));
    }

    @Test
    public void testCreateJsonArrayFromObject() {
        String array =
                "[{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"pass\":\"admin\"}]";
        assertEquals(
                JSONArray.fromObject(array), globalConfigurationService.createJsonArrayFromObject(jsonObjectNotNull));
    }

    @Test
    public void testInstantiateGlobalConfigData() {
        JSONObject json = new JSONObject();
        json.put(
                "sonarInstances",
                JSONArray.fromObject(
                        "[{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}]"));
        JSON globalDataConfig = (JSON) json.opt("sonarInstances");
        doNothing().when(spyGlobalConfigurationService).initGlobalDataConfig(globalDataConfig);
        assertEquals(sonarInstances, spyGlobalConfigurationService.instantiateGlobalConfigData(json));
    }

    @Test
    public void testInstantiateGlobalConfigDataWhenJsonIsNull() {
        JSONObject json = new JSONObject();
        doNothing().when(spyGlobalConfigurationService).initGlobalDataConfig(any(JSON.class));
        assertEquals(sonarInstances, spyGlobalConfigurationService.instantiateGlobalConfigData(json));
    }

    @Test
    public void testContainsGlobalConfigWithNameTrue() {
        String name = "Ime";
        SonarInstance sonarInstance = new SonarInstance();
        sonarInstance.setName("Ime");
        sonarInstances.add(sonarInstance);
        spyGlobalConfigurationService.setListOfGlobalConfigInstances(sonarInstances);
        assertTrue(spyGlobalConfigurationService.containsGlobalConfigWithName(name));
    }

    @Test
    public void testContainsGlobalConfigWithNameFalse() {
        String name = "Ime";
        SonarInstance sonarInstance = new SonarInstance();
        sonarInstance.setName("Ime3");
        sonarInstances.add(sonarInstance);
        spyGlobalConfigurationService.setListOfGlobalConfigInstances(sonarInstances);
        assertFalse(spyGlobalConfigurationService.containsGlobalConfigWithName(name));
    }
}
