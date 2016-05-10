package quality.gates.jenkins.plugin;

import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JobConfigurationServiceTest {

    private JobConfigurationService jobConfigurationService;

    @Mock
    private GlobalConfig globalConfig;

    @Mock
    private List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstances;

    @Mock
    GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    private JobConfigData jobConfigData;

    private JSONObject formData;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        jobConfigurationService = new JobConfigurationService();
        formData = new JSONObject();
        jobConfigData = new JobConfigData();
        formData.put("projectKey", "TestKey");
        jobConfigData.setProjectKey("TestKey");
        jobConfigData.setSonarInstanceName("TestName");
    }

    @Test
    public void testGetListBoxModelShouldReturnOneInstance(){
        createGlobalConfigData();
        globalConfigDataForSonarInstance.setName("First Instance");
        globalConfigDataForSonarInstances.add(globalConfigDataForSonarInstance);
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).fetchListOfGlobalConfigData();
        ListBoxModel returnList = jobConfigurationService.getListOfSonarInstanceNames(globalConfig);
        assertEquals("[First Instance=First Instance]", returnList.toString());
    }

    @Test
    public void testGetListBoxModelShouldReturnEmptyListBoxModel(){
        ListBoxModel returnList = jobConfigurationService.getListOfSonarInstanceNames(globalConfig);
        assertEquals("[]", returnList.toString());
    }

    @Test
    public void testGetListBoxModelShouldReturnMoreInstance(){
        createGlobalConfigData();
        globalConfigDataForSonarInstance.setName("First Instance");
        globalConfigDataForSonarInstances.add(globalConfigDataForSonarInstance);
        GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance1 = new GlobalConfigDataForSonarInstance();
        globalConfigDataForSonarInstance1.setName("Second Instance");
        globalConfigDataForSonarInstances.add(globalConfigDataForSonarInstance1);

        doReturn(globalConfigDataForSonarInstances).when(globalConfig).fetchListOfGlobalConfigData();
        ListBoxModel returnList = jobConfigurationService.getListOfSonarInstanceNames(globalConfig);
        assertEquals("[First Instance=First Instance, Second Instance=Second Instance]", returnList.toString());
    }

    @Test
    public void testNewInstanceSizeGreaterThanZeroAndDoesNotContainKey() {
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).fetchListOfGlobalConfigData();
        int greaterThanZero = 1;
        doReturn(greaterThanZero).when(globalConfigDataForSonarInstances).size();
        globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance();
        globalConfigDataForSonarInstance.setName("TestName");
        doReturn(globalConfigDataForSonarInstance).when(globalConfigDataForSonarInstances).get(0);
        JobConfigData returnedJobConfigData = jobConfigurationService.createJobConfigData(formData, globalConfig);
        verify(globalConfigDataForSonarInstances, times(1)).get(0);
        assertEquals(jobConfigData, returnedJobConfigData);
    }

    @Test
    public void testNewInstanceSizeGreaterThanZeroAndContainsKey() {
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).fetchListOfGlobalConfigData();
        int greaterThanZero = 1;
        doReturn(greaterThanZero).when(globalConfigDataForSonarInstances).size();
        String sonarInstanceName = "TestName";
        formData.put("sonarInstancesName", sonarInstanceName);
        JobConfigData returnedJobConfigData = jobConfigurationService.createJobConfigData(formData, globalConfig);
        assertEquals(jobConfigData, returnedJobConfigData);
    }

//    @Test
//    public void testNewInstanceSizeZero() {
//        doReturn(globalConfigDataForSonarInstances).when(globalConfig).getListOfGlobalConfigData();
//        doReturn(true).when(globalConfigDataForSonarInstances).isEmpty();
//        JobConfigData returnedJobConfigData = jobConfigurationService.createJobConfigData(formData, globalConfig);
//        assertEquals("", returnedJobConfigData.getGlobalConfigDataForSonarInstance().getName());
//    }

    protected void createGlobalConfigData() {
        globalConfigDataForSonarInstances = new ArrayList<>();
        globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance();
    }
}