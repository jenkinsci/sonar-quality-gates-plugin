package quality.gates.jenkins.plugin;

import net.sf.json.JSONArray;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class GlobalConfigurationServiceTest {

    private GlobalConfigurationService globalConfigurationService;
    private GlobalConfigurationService spyGlobalConfigurationService;
    @Mock
    private List<GlobalConfigDataForSonarInstance> listOfGlobalConfigData;
    private JSONObject jsonObjectNotNull;
    private JSON globalDataConfigs;
    private JSONArray jsonArray;

    @Before
    public void setUp(){
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
    public void testGetGlobalConfigsArrayWhenObject(){
        jsonArray = new JSONArray();
        doReturn(jsonArray).when(spyGlobalConfigurationService).createJsonArrayFromObject(JSONObject.fromObject(globalDataConfigs));
        String objectString = "{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}";
        globalDataConfigs = JSONSerializer.toJSON(objectString);
        jsonArray.add(globalDataConfigs);
        assertEquals(jsonArray, spyGlobalConfigurationService.getGlobalConfigsArray(globalDataConfigs));
    }

    @Test
    public void testGetGlobalConfigsArrayWhenArray(){
        String arrayString = "[{\"name\":\"Sonar1\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"},{\"name\":\"Sonar2\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}]";
        globalDataConfigs = JSONSerializer.toJSON(arrayString);
        jsonArray = JSONArray.class.cast(globalDataConfigs);
        assertEquals(jsonArray, globalConfigurationService.getGlobalConfigsArray(globalDataConfigs));
    }

    @Test
    public void testCreateJsonArrayFromObject(){
        String array = "[{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"pass\":\"admin\"}]";
        JSONArray jsonArray =  JSONArray.fromObject(array);
        assertEquals(jsonArray, globalConfigurationService.createJsonArrayFromObject(jsonObjectNotNull));
    }

    @Test
    public void testInstantiateGlobalConfigData(){
        JSONObject json = new JSONObject();
        json.put("listOfGlobalConfigData", JSONArray.fromObject("[{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}]"));
        JSON globalDataConfigs = (JSON) json.opt("listOfGlobalConfigData");
        doNothing().when(spyGlobalConfigurationService).initGlobalDataConfig(globalDataConfigs);
        assertEquals(listOfGlobalConfigData, spyGlobalConfigurationService.instantiateGlobalConfigData(json));
    }

    @Test
    public void testInstantiateGlobalConfigDataWhenJsonIsNull(){
        JSONObject json = new JSONObject();
        JSON globalDataConfigs = (JSON) json.opt("listOfGlobalConfigData");
        doNothing().when(spyGlobalConfigurationService).initGlobalDataConfig(globalDataConfigs);
        assertEquals(listOfGlobalConfigData, spyGlobalConfigurationService.instantiateGlobalConfigData(json));
    }

    @Test
    public void testContainsGlobalConfigWithNameTrue(){
        String name = "Ime";
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance();
        globalConfigDataForSonarInstance.setName("Ime");
        listOfGlobalConfigData.add(globalConfigDataForSonarInstance);
        spyGlobalConfigurationService.setListOfGlobalConfigInstances(listOfGlobalConfigData);
        assertTrue(spyGlobalConfigurationService.containsGlobalConfigWithName(name));
    }

    @Test
    public void testContainsGlobalConfigWithNameFalse(){
        String name = "Ime";
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance();
        globalConfigDataForSonarInstance.setName("Ime3");
        listOfGlobalConfigData.add(globalConfigDataForSonarInstance);
        spyGlobalConfigurationService.setListOfGlobalConfigInstances(listOfGlobalConfigData);
        assertFalse(spyGlobalConfigurationService.containsGlobalConfigWithName(name));
    }

    @Test
    public void testInitGlobalDataConfig(){
        JSONArray array = JSONArray.fromObject("[{\"name\":\"Sonar1\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"},{\"name\":\"Sonar2\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}]");
        doReturn(JSONArray.fromObject(array)).when(spyGlobalConfigurationService).getGlobalConfigsArray(any(JSON.class));
        doNothing().when(spyGlobalConfigurationService).addGlobalConfigDataForSonarInstance(any(JSONObject.class));
        spyGlobalConfigurationService.initGlobalDataConfig(any(JSON.class));
        verify(spyGlobalConfigurationService, times(1)).initGlobalDataConfig(any(JSON.class));
    }
}