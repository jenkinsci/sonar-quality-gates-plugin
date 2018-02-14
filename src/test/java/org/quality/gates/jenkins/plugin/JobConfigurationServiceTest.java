package org.quality.gates.jenkins.plugin;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quality.gates.jenkins.plugin.enumeration.BuildStatusEnum;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JobConfigurationServiceTest {

    private JobConfigurationService jobConfigurationService;

    @Mock
    private GlobalConfig globalConfig;

    @Mock
    private List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstances;

    @Mock
    GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    private JSONObject formData;

    @Mock
    private JobConfigData jobConfigData;

    @Mock
    private AbstractBuild build;

    @Mock
    private BuildListener listener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jobConfigurationService = new JobConfigurationService();
        formData = new JSONObject();
        formData.put("projectKey", "TestKey");
        formData.put("buildStatus", BuildStatusEnum.FAILED.toString());
    }

    @Test
    public void testGetListBoxModelShouldReturnOneInstance() {

        createGlobalConfigData();
        globalConfigDataForSonarInstance.setName("First Instance");
        globalConfigDataForSonarInstances.add(globalConfigDataForSonarInstance);
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).fetchListOfGlobalConfigData();
        ListBoxModel returnList = jobConfigurationService.getListOfSonarInstanceNames(globalConfig);
        assertEquals("[First Instance=First Instance]", returnList.toString());
    }

    @Test
    public void testGetListBoxModelShouldReturnEmptyListBoxModel() {

        ListBoxModel returnList = jobConfigurationService.getListOfSonarInstanceNames(globalConfig);
        assertEquals("[]", returnList.toString());
    }

    @Test
    public void testGetListBoxModelShouldReturnMoreInstance() {

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

        jobConfigData = new JobConfigData();
        jobConfigData.setProjectKey("TestKey");
        jobConfigData.setSonarInstanceName("TestName");
        jobConfigData.setBuildStatus(BuildStatusEnum.FAILED);
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

        jobConfigData = new JobConfigData();
        jobConfigData.setProjectKey("TestKey");
        jobConfigData.setSonarInstanceName("TestName");
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).fetchListOfGlobalConfigData();
        int greaterThanZero = 1;
        doReturn(greaterThanZero).when(globalConfigDataForSonarInstances).size();
        String sonarInstanceName = "TestName";
        formData.put("sonarInstancesName", sonarInstanceName);
        JobConfigData returnedJobConfigData = jobConfigurationService.createJobConfigData(formData, globalConfig);
        assertEquals(jobConfigData, returnedJobConfigData);
    }

    protected void createGlobalConfigData() {

        globalConfigDataForSonarInstances = new ArrayList<>();
        globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance();
    }

    @Test
    public void testIfProjectKeyEmpty() throws Exception {

        String key = "";
        doReturn(key).when(jobConfigData).getProjectKey();

        try {
            jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        } catch (QGException e) {
            assertTrue(e.toString().contains("Empty project key."));
        }
    }

    @Test
    public void testIfProjectKeyStartsWithDolarSignAndVarIsFound() throws Exception {

        String key = "$PROJECT_KEY";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn("EnvVariable").when(envVars).get("PROJECT_KEY");
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertTrue(returnedData.getProjectKey().equals("EnvVariable"));
    }

    @Test
    public void testIfProjectKeyStartsWithDolarSignAndVarIsNotFound() throws Exception {

        String key = "$";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn(null).when(envVars).get(anyString());

        try {
            jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        } catch (QGException e) {
            assertTrue(e.toString().contains("Environment variable with name '' was not found."));
        }
    }

    @Test
    public void testIfProjectKeyStartsWithDolarSignAndHasBracketsVarIsFound() throws Exception {

        String key = "${PROJECT_KEY}";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn("EnvVariable").when(envVars).get("PROJECT_KEY");
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertTrue(returnedData.getProjectKey().equals("EnvVariable"));
    }

    @Test
    public void testNotEnvironmentVariable() {

        String key = "NormalString";
        doReturn(key).when(jobConfigData).getProjectKey();
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertTrue(returnedData.getProjectKey().equals("NormalString"));
    }

    @Test(expected = QGException.class)
    public void testEnvironmentThrowsIOException() throws Exception {

        String key = "$";
        doReturn(key).when(jobConfigData).getProjectKey();
        IOException exception = mock(IOException.class);
        doThrow(exception).when(build).getEnvironment(listener);
        jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
    }

    @Test(expected = QGException.class)
    public void testEnvironmentThrowsInterruptedException() throws Exception {

        String key = "$";
        doReturn(key).when(jobConfigData).getProjectKey();
        InterruptedIOException exception = mock(InterruptedIOException.class);
        doThrow(exception).when(build).getEnvironment(listener);
        jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
    }

    @Test
    public void resolveEmbeddedEnvVariablesWithoutBraces() throws Exception {

        String key = "$PROJECT_KEY";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn("$FOO:$BAR").when(envVars).get("PROJECT_KEY");
        doReturn("Foo-Value").when(envVars).get("FOO");
        doReturn("Bar-Value").when(envVars).get("BAR");
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertTrue(returnedData.getProjectKey().equals("Foo-Value:Bar-Value"));
    }

    @Test(expected = QGException.class)
    public void embeddedEnvVariablesNotFound() throws Exception {

        String key = "${PROJECT_KEY}";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn(null).when(envVars).get("PROJECT_KEY");
        jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
    }

    @Test
    public void resolveEmbeddedEnvVariables() throws Exception {

        String key = "${PROJECT_KEY}";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn("$FOO:$BAR").when(envVars).get("PROJECT_KEY");
        doReturn("Foo-Value").when(envVars).get("FOO");
        doReturn("Bar-Value").when(envVars).get("BAR");
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertTrue(returnedData.getProjectKey().equals("Foo-Value:Bar-Value"));
    }

    @Test
    public void resolveMultiLevelEmbeddedEnvVariables() throws Exception {

        String key = "${PROJECT_KEY}";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn("$FOO:$BAR").when(envVars).get("PROJECT_KEY");
        doReturn("Foo-${VERSION}").when(envVars).get("FOO");
        doReturn("Bar").when(envVars).get("BAR");
        doReturn("12").when(envVars).get("VERSION");
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertTrue(returnedData.getProjectKey().equals("Foo-12:Bar"));
    }
}
