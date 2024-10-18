package org.quality.gates.jenkins.plugin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import hudson.model.BuildListener;
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
                        any(JobConfigData.class), any(SonarInstance.class), any(BuildListener.class));
        doReturn(true).when(qualityGatesStatus).hasStatusGreen();
        assertTrue(buildDecision.getStatus(sonarInstance, jobConfigData, listener));
    }

    @Test
    public void testGetStatusFalse() throws JSONException, InterruptedException {
        doReturn(qualityGatesStatus)
                .when(qualityGatesProvider)
                .getAPIResultsForQualityGates(
                        any(JobConfigData.class), any(SonarInstance.class), any(BuildListener.class));
        doReturn(false).when(qualityGatesStatus).hasStatusGreen();
        assertFalse(buildDecision.getStatus(sonarInstance, jobConfigData, listener));
    }

    @Test(expected = QGException.class)
    public void testGetStatusThrowJSONException() throws JSONException, InterruptedException {
        JSONException jsonException = Mockito.mock(JSONException.class);
        when(qualityGatesProvider.getAPIResultsForQualityGates(
                        any(JobConfigData.class), any(SonarInstance.class), any(BuildListener.class)))
                .thenThrow(jsonException);
        buildDecision.getStatus(sonarInstance, jobConfigData, listener);
    }

    @Test
    public void testChooseSonarInstanceIfListIsEmpty() {
        String emptyString = "";
        List<SonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchSonarInstances();
        SonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertTrue(returnedInstance.getName().equals(emptyString));
        assertTrue(returnedInstance.getPass().getPlainText().equals(emptyString));
        assertTrue(returnedInstance.getUrl().equals(emptyString));
        assertTrue(returnedInstance.getUsername().equals(emptyString));
    }

    @Test
    public void testChooseSonarInstanceIfListHasOneInstance() {
        List<SonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        SonarInstance singleInstance = new SonarInstance("TestName", "TestUrl", "TestUsername", "TestPass");
        globalConfigDataForSonarInstanceList.add(singleInstance);
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchSonarInstances();
        SonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertTrue(returnedInstance.getName().equals("TestName"));
        assertTrue(returnedInstance.getUrl().equals("TestUrl"));
        assertTrue(returnedInstance.getUsername().equals("TestUsername"));
    }

    @Test
    public void testChooseSonarInstanceIfListHasMultipleInstancesAndNameMatches() {
        List<SonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        SonarInstance firstInstance = new SonarInstance("TestName", "TestUrl", "TestUsername", "TestPass");
        SonarInstance secondInstance = new SonarInstance("TestName1", "TestUrl1", "TestUsername1", "TestPass1");
        globalConfigDataForSonarInstanceList.add(firstInstance);
        globalConfigDataForSonarInstanceList.add(secondInstance);
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchSonarInstances();
        doReturn(secondInstance).when(globalConfig).getSonarInstanceByName("TestName1");
        doReturn("TestName1").when(jobConfigData).getSonarInstanceName();
        SonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertTrue(returnedInstance.getName().equals("TestName1"));
        assertTrue(returnedInstance.getUrl().equals("TestUrl1"));
        assertTrue(returnedInstance.getUsername().equals("TestUsername1"));
    }

    @Test
    public void testChooseSonarInstanceIfListHasMultipleInstancesAndNameDoesNotMatch() {
        List<SonarInstance> globalConfigDataForSonarInstanceList = new ArrayList<>();
        SonarInstance firstInstance = new SonarInstance("TestName", "TestUrl", "TestUsername", "TestPass");
        SonarInstance secondInstance = new SonarInstance("TestName1", "TestUrl1", "TestUsername1", "TestPass1");
        globalConfigDataForSonarInstanceList.add(firstInstance);
        globalConfigDataForSonarInstanceList.add(secondInstance);
        doReturn(globalConfigDataForSonarInstanceList).when(globalConfig).fetchSonarInstances();
        doReturn(null).when(globalConfig).getSonarInstanceByName("RandomName");
        doReturn("RandomName").when(jobConfigData).getSonarInstanceName();
        SonarInstance returnedInstance = buildDecision.chooseSonarInstance(globalConfig, jobConfigData);
        assertTrue(returnedInstance == null);
    }
}
