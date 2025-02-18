package org.quality.gates.jenkins.plugin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quality.gates.jenkins.plugin.enumeration.BuildStatusEnum;

class JobConfigurationServiceTest {

    private JobConfigurationService jobConfigurationService;

    @Mock
    private GlobalSonarQualityGatesConfiguration globalConfig;

    @Mock
    private List<SonarInstance> globalConfigDataForSonarInstances;

    @Mock
    private SonarInstance sonarInstance;

    private JSONObject formData;

    @Mock
    private JobConfigData jobConfigData;

    @Mock
    private AbstractBuild<?, ?> build;

    @Mock
    private BuildListener listener;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        jobConfigurationService = new JobConfigurationService();
        formData = new JSONObject();
        formData.put("projectKey", "TestKey");
        formData.put("buildStatus", BuildStatusEnum.FAILED.toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetListBoxModelShouldReturnOneInstance() {
        createGlobalConfigData();
        sonarInstance.setName("First Instance");
        globalConfigDataForSonarInstances.add(sonarInstance);
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).fetchSonarInstances();
        ListBoxModel returnList = jobConfigurationService.getListOfSonarInstanceNames(globalConfig);
        assertEquals("[First Instance=First Instance]", returnList.toString());
    }

    @Test
    void testGetListBoxModelShouldReturnEmptyListBoxModel() {
        ListBoxModel returnList = jobConfigurationService.getListOfSonarInstanceNames(globalConfig);
        assertEquals("[]", returnList.toString());
    }

    @Test
    void testGetListBoxModelShouldReturnMoreInstance() {
        createGlobalConfigData();
        sonarInstance.setName("First Instance");
        globalConfigDataForSonarInstances.add(sonarInstance);
        SonarInstance globalConfigDataForSonarInstance1 = new SonarInstance();
        globalConfigDataForSonarInstance1.setName("Second Instance");
        globalConfigDataForSonarInstances.add(globalConfigDataForSonarInstance1);

        doReturn(globalConfigDataForSonarInstances).when(globalConfig).fetchSonarInstances();
        ListBoxModel returnList = jobConfigurationService.getListOfSonarInstanceNames(globalConfig);
        assertEquals("[First Instance=First Instance, Second Instance=Second Instance]", returnList.toString());
    }

    @Test
    void testNewInstanceSizeGreaterThanZeroAndDoesNotContainKey() {
        jobConfigData = new JobConfigData();
        jobConfigData.setProjectKey("TestKey");
        jobConfigData.setSonarInstanceName("TestName");
        jobConfigData.setBuildStatus(BuildStatusEnum.FAILED);
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).fetchSonarInstances();
        int greaterThanZero = 1;
        doReturn(greaterThanZero).when(globalConfigDataForSonarInstances).size();
        sonarInstance = new SonarInstance();
        sonarInstance.setName("TestName");
        doReturn(sonarInstance).when(globalConfigDataForSonarInstances).get(0);
        JobConfigData returnedJobConfigData = jobConfigurationService.createJobConfigData(formData, globalConfig);
        verify(globalConfigDataForSonarInstances, times(1)).get(0);
        assertEquals(jobConfigData, returnedJobConfigData);
    }

    @Test
    void testNewInstanceSizeGreaterThanZeroAndContainsKey() {
        jobConfigData = new JobConfigData();
        jobConfigData.setProjectKey("TestKey");
        jobConfigData.setSonarInstanceName("TestName");
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).fetchSonarInstances();
        int greaterThanZero = 1;
        doReturn(greaterThanZero).when(globalConfigDataForSonarInstances).size();
        String sonarInstanceName = "TestName";
        formData.put("sonarInstancesName", sonarInstanceName);
        JobConfigData returnedJobConfigData = jobConfigurationService.createJobConfigData(formData, globalConfig);
        assertEquals(jobConfigData, returnedJobConfigData);
    }

    protected void createGlobalConfigData() {
        globalConfigDataForSonarInstances = new ArrayList<>();
        sonarInstance = new SonarInstance();
    }

    @Test
    void testIfProjectKeyEmpty() {
        String key = "";
        doReturn(key).when(jobConfigData).getProjectKey();

        try {
            jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        } catch (QGException e) {
            assertTrue(e.toString().contains("Empty project key."));
        }
    }

    @Test
    void testIfProjectKeyStartsWithDollarSignAndVarIsFound() throws Exception {
        String key = "$PROJECT_KEY";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn("EnvVariable").when(envVars).get("PROJECT_KEY");
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertEquals("EnvVariable", returnedData.getProjectKey());
    }

    @Test
    void testIfProjectKeyStartsWithDollarSignAndVarIsNotFound() throws Exception {
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
    void testIfProjectKeyStartsWithDollarSignAndHasBracketsVarIsFound() throws Exception {
        String key = "${PROJECT_KEY}";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn("EnvVariable").when(envVars).get("PROJECT_KEY");
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertEquals("EnvVariable", returnedData.getProjectKey());
    }

    @Test
    void testNotEnvironmentVariable() {
        String key = "NormalString";
        doReturn(key).when(jobConfigData).getProjectKey();
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertEquals("NormalString", returnedData.getProjectKey());
    }

    @Test
    void testEnvironmentThrowsIOException() throws IOException, InterruptedException {
        String key = "$";
        doReturn(key).when(jobConfigData).getProjectKey();
        IOException exception = mock(IOException.class);
        doThrow(exception).when(build).getEnvironment(listener);
        assertThrows(
                QGException.class,
                () -> jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener));
    }

    @Test
    void testEnvironmentThrowsInterruptedException() throws IOException, InterruptedException {
        String key = "$";
        doReturn(key).when(jobConfigData).getProjectKey();
        InterruptedIOException exception = mock(InterruptedIOException.class);
        doThrow(exception).when(build).getEnvironment(listener);
        assertThrows(
                QGException.class,
                () -> jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener));
    }

    @Test
    void resolveEmbeddedEnvVariablesWithoutBraces() throws Exception {
        String key = "$PROJECT_KEY";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn("$FOO:$BAR").when(envVars).get("PROJECT_KEY");
        doReturn("Foo-Value").when(envVars).get("FOO");
        doReturn("Bar-Value").when(envVars).get("BAR");
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertEquals("Foo-Value:Bar-Value", returnedData.getProjectKey());
    }

    @Test
    void embeddedEnvVariablesNotFound() throws IOException, InterruptedException {
        String key = "${PROJECT_KEY}";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn(null).when(envVars).get("PROJECT_KEY");
        assertThrows(
                QGException.class,
                () -> jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener));
    }

    @Test
    void resolveEmbeddedEnvVariables() throws Exception {
        String key = "${PROJECT_KEY}";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn("$FOO:$BAR").when(envVars).get("PROJECT_KEY");
        doReturn("Foo-Value").when(envVars).get("FOO");
        doReturn("Bar-Value").when(envVars).get("BAR");
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertEquals("Foo-Value:Bar-Value", returnedData.getProjectKey());
    }

    @Test
    void resolveMultiLevelEmbeddedEnvVariables() throws Exception {
        String key = "${PROJECT_KEY}";
        doReturn(key).when(jobConfigData).getProjectKey();
        EnvVars envVars = mock(EnvVars.class);
        doReturn(envVars).when(build).getEnvironment(listener);
        doReturn("$FOO:$BAR").when(envVars).get("PROJECT_KEY");
        doReturn("Foo-${VERSION}").when(envVars).get("FOO");
        doReturn("Bar").when(envVars).get("BAR");
        doReturn("12").when(envVars).get("VERSION");
        JobConfigData returnedData = jobConfigurationService.checkProjectKeyIfVariable(jobConfigData, build, listener);
        assertEquals("Foo-12:Bar", returnedData.getProjectKey());
    }
}
