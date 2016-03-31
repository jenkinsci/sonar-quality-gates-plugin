package quality.gates.jenkins.plugin;

import net.sf.json.JSONArray;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.util.JSONTokener;
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
    private String jsonStr;
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
        jsonObjectNotNull.put("password", "admin");
        jsonStr = "{\n" +
                  "listOfGlobalConfigData:"+
                        "[{\n" +
                        "name: \"Sonar1\",\n" +
                        "url: \"\",\n" +
                        "account:\"\",\n" +
                        "password:\"\"},\n" +
                        "{\n" +
                        "name:\"Sonar2\",\n" +
                        "url:\"\",\n" +
                        "account:\"\",\n" +
                        "password:\"\"" +
                        "}]}";

    }

    @Test
    public void testIsNotNullJsonWhenJsonIsNotNull(){
        assertTrue(globalConfigurationService.isNotNullJson(jsonObjectNotNull));
    }

    @Test
    public void testIsNotNullJsonWhenJsonIsNull(){
        JSONObject jsonObject = null;
        assertFalse(globalConfigurationService.isNotNullJson(jsonObject));
    }

    @Test
    public void testIsNotEmptyOrNullWhenNotNull(){
        JSON json = (JSON) new JSONTokener(jsonStr).nextValue();
        assertTrue(globalConfigurationService.isNotEmptyOrNull(json));
    }

    @Test
    public void testIsNotEmptyOrNullWhenNull(){
        JSON json = null;
        assertFalse(globalConfigurationService.isNotEmptyOrNull(json));
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
        String array = "[{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}]";
        JSONArray jsonArray =  JSONArray.fromObject(array);
        assertEquals(jsonArray, globalConfigurationService.createJsonArrayFromObject(jsonObjectNotNull));
    }

    @Test
    public void testInstantiateGlobalConfigData(){
        doNothing().when(spyGlobalConfigurationService).setGlobalDataConfigWhenNotNull(any(JSONObject.class));
        assertEquals(listOfGlobalConfigData, spyGlobalConfigurationService.instantiateGlobalConfigData(JSONObject.fromObject(jsonStr)));
    }

    @Test
    public void testInstantiateGlobalConfigData2(){
        doNothing().when(spyGlobalConfigurationService).setGlobalDataConfigWhenNotNull(null);
        assertEquals(listOfGlobalConfigData, spyGlobalConfigurationService.instantiateGlobalConfigData(null));
    }

    @Test
    public void testSetGlobalDataConfigWhenNotNull(){
        JSONObject json = new JSONObject();
        json.put("listOfGlobalConfigData", JSONArray.fromObject("[{\"name\":\"Sonar\",\"url\":\"http://localhost:9000\",\"account\":\"admin\",\"password\":\"admin\"}]"));
        JSON globalDataConfigs = (JSON) json.opt("listOfGlobalConfigData");
        doNothing().when(spyGlobalConfigurationService).initGlobalDataConfig(globalDataConfigs);
        spyGlobalConfigurationService.setGlobalDataConfigWhenNotNull(json);
        verify(spyGlobalConfigurationService, times(1)).initGlobalDataConfig(globalDataConfigs);
    }

    @Test
    public void testSetGlobalDataConfigWhenNotNullWhenNull(){
        JSONObject json = new JSONObject();
        doNothing().when(spyGlobalConfigurationService).initGlobalDataConfig(null);
        spyGlobalConfigurationService.setGlobalDataConfigWhenNotNull(json);
        verify(spyGlobalConfigurationService, times(0)).initGlobalDataConfig(null);
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
