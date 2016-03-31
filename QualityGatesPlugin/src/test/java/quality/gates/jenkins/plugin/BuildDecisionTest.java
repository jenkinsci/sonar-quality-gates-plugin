package quality.gates.jenkins.plugin;

import quality.gates.sonar.api.QualityGatesProvider;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import quality.gates.sonar.api.QualityGatesStatus;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class BuildDecisionTest {

    private BuildDecision buildDecision;

    @Mock
    QualityGatesProvider qualityGatesProvider;

    @Mock
    QualityGatesStatus qualityGatesStatus;

    @Mock
    JobConfigData jobConfigData;

    @Mock
    GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        buildDecision = new BuildDecision(qualityGatesProvider);
    }

    @Test
    public void testGetStatusTrue() throws Exception {
        doReturn(qualityGatesStatus).when(qualityGatesProvider).getAPIResultsForQualityGates(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class));
        doReturn(true).when(qualityGatesStatus).hasStatusGreen();
        assertTrue(buildDecision.getStatus(jobConfigData));
    }

    @Test
    public void testGetStatusFalse() throws Exception {
        doReturn(qualityGatesStatus).when(qualityGatesProvider).getAPIResultsForQualityGates(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class));
        doReturn(false).when(qualityGatesStatus).hasStatusGreen();
        assertFalse(buildDecision.getStatus(jobConfigData));
    }

    @Test(expected = QGException.class)
    public void testGetStatusThrowJSONException() throws Exception {
        JSONException jsonException = Mockito.mock(JSONException.class);
        when(qualityGatesProvider.getAPIResultsForQualityGates(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class))).thenThrow(jsonException);
        buildDecision.getStatus(jobConfigData);
    }
}