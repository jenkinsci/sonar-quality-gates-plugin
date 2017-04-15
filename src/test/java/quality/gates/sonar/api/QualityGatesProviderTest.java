package quality.gates.sonar.api;

import hudson.model.BuildListener;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import quality.gates.jenkins.plugin.JobConfigData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

public class QualityGatesProviderTest {

    @InjectMocks
    private QualityGatesProvider qualityGatesProvider;

    @Mock
    private QualityGateResponseParser qualityGateResponseParser;

    @Mock
    private SonarHttpRequester sonarHttpRequester;

    @Mock
    private JobConfigData jobConfigData;

    @Mock
    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @Mock
    private SonarInstanceValidationService sonarInstanceValidationService;

    @Mock
    private BuildListener listener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        qualityGatesProvider = new QualityGatesProvider(qualityGateResponseParser, sonarHttpRequester, sonarInstanceValidationService);
    }

    @Ignore
    @Test
    public void testGetAPIResultsForQualityGates() throws JSONException, InterruptedException {

        QualityGatesStatus qualityGatesStatus = new QualityGatesStatus("OK");

        doReturn("").when(globalConfigDataForSonarInstance).getName();
        doReturn("").when(globalConfigDataForSonarInstance).getUsername();
        doReturn("").when(globalConfigDataForSonarInstance).getPass();
        doReturn("").when(globalConfigDataForSonarInstance).getSonarUrl();
        doReturn("").when(jobConfigData).getProjectKey();
        doReturn("").when(sonarHttpRequester).getAPIInfo(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class));
        doReturn(globalConfigDataForSonarInstance).when(sonarInstanceValidationService).validateData(globalConfigDataForSonarInstance);
        doReturn(qualityGatesStatus).when(qualityGateResponseParser).getQualityGateResultFromJSON(anyString());

        assertEquals(qualityGatesStatus, qualityGatesProvider.getAPIResultsForQualityGates(jobConfigData, globalConfigDataForSonarInstance, listener));
    }

    @Ignore
    @Test
    public void testGetAPIResultsForQualityGatesNotEqualStatuses() throws JSONException, InterruptedException {

        QualityGatesStatus qualityGatesStatus = new QualityGatesStatus("OK");

        doReturn("").when(jobConfigData).getProjectKey();
        doReturn("").when(sonarHttpRequester).getAPIInfo(any(JobConfigData.class), any(GlobalConfigDataForSonarInstance.class));
        doReturn(globalConfigDataForSonarInstance).when(sonarInstanceValidationService).validateData(globalConfigDataForSonarInstance);
        doReturn(new QualityGatesStatus("ERROR")).when(qualityGateResponseParser).getQualityGateResultFromJSON(anyString());

        assertNotEquals(qualityGatesStatus, qualityGatesProvider.getAPIResultsForQualityGates(jobConfigData, globalConfigDataForSonarInstance, listener));
    }
}
