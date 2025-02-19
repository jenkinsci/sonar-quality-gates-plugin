package org.quality.gates.jenkins.plugin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import hudson.model.BuildListener;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quality.gates.sonar.api.QualityGatesProvider;
import org.quality.gates.sonar.api.QualityGatesStatus;

class BuildDecisionTest {

    private BuildDecision buildDecision;

    @Mock
    QualityGatesProvider qualityGatesProvider;

    @Mock
    QualityGatesStatus qualityGatesStatus;

    @Mock
    JobConfigData jobConfigData;

    @Mock
    SonarInstance sonarInstance;

    @Mock
    GlobalSonarQualityGatesConfiguration globalConfig;

    @Mock
    private BuildListener listener;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        buildDecision = new BuildDecision(qualityGatesProvider);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetStatusTrue() throws JSONException, InterruptedException {
        doReturn(qualityGatesStatus)
                .when(qualityGatesProvider)
                .getAPIResultsForQualityGates(
                        any(JobConfigData.class), any(SonarInstance.class), any(BuildListener.class));
        doReturn(true).when(qualityGatesStatus).hasStatusGreen();
        assertTrue(buildDecision.getStatus(sonarInstance, jobConfigData, listener));
    }

    @Test
    void testGetStatusFalse() throws JSONException, InterruptedException {
        doReturn(qualityGatesStatus)
                .when(qualityGatesProvider)
                .getAPIResultsForQualityGates(
                        any(JobConfigData.class), any(SonarInstance.class), any(BuildListener.class));
        doReturn(false).when(qualityGatesStatus).hasStatusGreen();
        assertFalse(buildDecision.getStatus(sonarInstance, jobConfigData, listener));
    }

    @Test
    void testGetStatusThrowJSONException() throws InterruptedException {
        JSONException jsonException = Mockito.mock(JSONException.class);
        when(qualityGatesProvider.getAPIResultsForQualityGates(
                        any(JobConfigData.class), any(SonarInstance.class), any(BuildListener.class)))
                .thenThrow(jsonException);
        assertThrows(QGException.class, () -> buildDecision.getStatus(sonarInstance, jobConfigData, listener));
    }

    @Test
    void testChooseSonarInstanceIfListIsEmpty() {
        String emptyString = "";
        List<SonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchSonarInstances();
        SonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertEquals(emptyString, returnedInstance.getName());
        assertNull(returnedInstance.getPass());
        assertEquals(emptyString, returnedInstance.getUrl());
        assertEquals(emptyString, returnedInstance.getUsername());
    }

    @Test
    void testChooseSonarInstanceIfListHasOneInstance() {
        List<SonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        SonarInstance singleInstance = new SonarInstance("TestName", "TestUrl", "TestUsername", "TestPass");
        globalConfigDataForSonarInstanceList.add(singleInstance);
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchSonarInstances();
        SonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertEquals("TestName", returnedInstance.getName());
        assertEquals("TestUrl", returnedInstance.getUrl());
        assertEquals("TestUsername", returnedInstance.getUsername());
    }

    @Test
    void testChooseSonarInstanceIfListHasMultipleInstancesAndNameMatches() {
        List<SonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        SonarInstance firstInstance = new SonarInstance("TestName", "TestUrl", "TestUsername", "TestPass");
        SonarInstance secondInstance = new SonarInstance("TestName1", "TestUrl1", "TestUsername1", "TestPass1");
        globalConfigDataForSonarInstanceList.add(firstInstance);
        globalConfigDataForSonarInstanceList.add(secondInstance);
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchSonarInstances();
        doReturn(secondInstance).when(globalConfig).getSonarInstanceByName("TestName1");
        doReturn("TestName1").when(jobConfigData).getSonarInstanceName();
        SonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertEquals("TestName1", returnedInstance.getName());
        assertEquals("TestUrl1", returnedInstance.getUrl());
        assertEquals("TestUsername1", returnedInstance.getUsername());
    }

    @Test
    void testChooseSonarInstanceIfListHasMultipleInstancesAndNameDoesNotMatch() {
        List<SonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        SonarInstance firstInstance = new SonarInstance("TestName", "TestUrl", "TestUsername", "TestPass");
        SonarInstance secondInstance = new SonarInstance("TestName1", "TestUrl1", "TestUsername1", "TestPass1");
        globalConfigDataForSonarInstanceList.add(firstInstance);
        globalConfigDataForSonarInstanceList.add(secondInstance);
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchSonarInstances();
        doReturn(null).when(globalConfig).getSonarInstanceByName("RandomName");
        doReturn("RandomName").when(jobConfigData).getSonarInstanceName();
        SonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertNull(returnedInstance);
    }
}
