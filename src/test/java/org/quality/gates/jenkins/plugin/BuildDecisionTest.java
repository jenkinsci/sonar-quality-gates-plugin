package org.quality.gates.jenkins.plugin;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quality.gates.sonar.api.QualityGatesProvider;
import org.quality.gates.sonar.api.QualityGatesStatus;

public class BuildDecisionTest {

    @Mock
    QualityGatesProvider qualityGatesProvider;

    @Mock
    QualityGatesStatus qualityGatesStatus;

    @Mock
    JobConfigData jobConfigData;

    @Mock
    GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @Mock
    GlobalConfig globalConfig;

    @Mock
    private BuildListener listener;

    @Mock
    private AbstractBuild<?, ?> run;

    private BuildDecision buildDecision;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        buildDecision = new BuildDecision(qualityGatesProvider);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetStatusTrue() throws JSONException, InterruptedException {
        doReturn(qualityGatesStatus)
                .when(qualityGatesProvider)
                .getAPIResultsForQualityGates(
                        any(JobConfigData.class),
                        any(GlobalConfigDataForSonarInstance.class),
                        any(BuildListener.class),
                        any(Run.class));
        doReturn(true).when(qualityGatesStatus).hasStatusGreen();
        assertTrue(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData, listener, run));
    }

    @Test
    public void testGetStatusFalse() throws JSONException, InterruptedException {
        doReturn(qualityGatesStatus)
                .when(qualityGatesProvider)
                .getAPIResultsForQualityGates(
                        any(JobConfigData.class),
                        any(GlobalConfigDataForSonarInstance.class),
                        any(BuildListener.class),
                        any(Run.class));
        doReturn(false).when(qualityGatesStatus).hasStatusGreen();
        assertFalse(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData, listener, run));
    }

    @Test(expected = QGException.class)
    public void testGetStatusThrowJSONException() throws JSONException, InterruptedException {
        JSONException jsonException = Mockito.mock(JSONException.class);
        when(qualityGatesProvider.getAPIResultsForQualityGates(
                        any(JobConfigData.class),
                        any(GlobalConfigDataForSonarInstance.class),
                        any(BuildListener.class),
                        any(Run.class)))
                .thenThrow(jsonException);
        buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData, listener, run);
    }

    @Test
    public void testChooseSonarInstanceIfListIsEmpty() {
        String emptyString = "";
        List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchListOfGlobalConfigData();
        GlobalConfigDataForSonarInstance returnedInstance =
                buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertEquals(returnedInstance.getName(), emptyString);
        assertEquals(returnedInstance.getSonarCredentialsId(), emptyString);
        assertEquals(returnedInstance.getSonarUrl(), emptyString);
    }

    @Test
    public void testChooseSonarInstanceIfListHasOneInstance() {
        var expectedName = "TestName";
        var expectedUrl = "TestUrl";
        var expectedCredentialsId = "TestCredentialsId";
        var expectedTimeToWait = 100;
        var expectedMaxWaitTime = 200;

        var globalConfigDataForSonarInstanceList = new ArrayList<GlobalConfigDataForSonarInstance>();
        var singleInstance = new GlobalConfigDataForSonarInstance(
                expectedName, expectedUrl, expectedCredentialsId, expectedTimeToWait, expectedMaxWaitTime);
        globalConfigDataForSonarInstanceList.add(singleInstance);

        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchListOfGlobalConfigData();

        var result = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);

        assertEquals(expectedName, result.getName());
        assertEquals(expectedUrl, result.getSonarUrl());
        assertEquals(expectedCredentialsId, result.getSonarCredentialsId());
        assertEquals(expectedTimeToWait, result.getTimeToWait());
        assertEquals(expectedMaxWaitTime, result.getMaxWaitTime());
    }

    @Test
    public void testChooseSonarInstanceIfListHasMultipleInstancesAndNameMatches() {
        List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        GlobalConfigDataForSonarInstance firstInstance =
                new GlobalConfigDataForSonarInstance("TestName", "TestUrl", "CredentialsId", 0, 0);
        GlobalConfigDataForSonarInstance secondInstance =
                new GlobalConfigDataForSonarInstance("TestName1", "TestUrl1", "CredentialsId1", 0, 0);
        globalConfigDataForSonarInstanceList.add(firstInstance);
        globalConfigDataForSonarInstanceList.add(secondInstance);

        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchListOfGlobalConfigData();
        doReturn(secondInstance).when(globalConfig).getSonarInstanceByName("TestName1");
        doReturn("TestName1").when(jobConfigData).getSonarInstanceName();

        var returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);

        assertEquals("TestName1", returnedInstance.getName());
        assertEquals("TestUrl1", returnedInstance.getSonarUrl());
        assertEquals("CredentialsId1", returnedInstance.getSonarCredentialsId());
    }

    @Test
    public void testChooseSonarInstanceIfListHasMultipleInstancesAndNameDoesNotMatch() {
        var globalConfigDataForSonarInstanceList = new ArrayList<GlobalConfigDataForSonarInstance>();
        var firstInstance = new GlobalConfigDataForSonarInstance("TestName", "TestUrl", "CredentialsId", 0, 0);
        var secondInstance = new GlobalConfigDataForSonarInstance("TestName1", "TestUrl1", "CredentialsId1", 0, 0);

        globalConfigDataForSonarInstanceList.add(firstInstance);
        globalConfigDataForSonarInstanceList.add(secondInstance);
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchListOfGlobalConfigData();
        doReturn(null).when(globalConfig).getSonarInstanceByName("RandomName");
        doReturn("RandomName").when(jobConfigData).getSonarInstanceName();

        var returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);

        assertNull(returnedInstance);
    }
}
