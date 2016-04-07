package quality.gates.jenkins.plugin;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QGPublisherTest {

    private QGPublisher publisher;

    @Mock
    private BuildDecision buildDecision;

    @Mock
    private JobConfigData jobConfigData;

    @Mock
    private JobExecutionService jobExecutionService;

    @Mock
    private BuildListener buildListener;

    @Mock
    private PrintStream printStream;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private AbstractBuild abstractBuild;

    @Mock
    private Launcher launcher;

    @Mock
    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    @Mock
    private GlobalConfig globalConfig;

    private QGPublisherDescriptor publisherDescriptor;

    @Mock
    JobConfigurationService jobConfigurationService;

    @Mock
    List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstances;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        publisher = new QGPublisher(jobConfigData, buildDecision, jobExecutionService);
        publisherDescriptor = new QGPublisherDescriptor(globalConfig, jobConfigurationService);
        doReturn(printStream).when(buildListener).getLogger();
        doReturn(printWriter).when(buildListener).error(anyString(), anyObject());
    }

    @Test
    public void testPrebuildShouldFail() {
        doReturn(publisherDescriptor).when(jobExecutionService).getPublisherDescriptor();
        doReturn(false).when(jobExecutionService).hasGlobalConfigDataWithSameName(jobConfigData, globalConfig);
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).getListOfGlobalConfigData();
        doReturn(1).when(globalConfigDataForSonarInstances).size();
        doReturn(globalConfigDataForSonarInstance).when(jobConfigData).getGlobalConfigDataForSonarInstance();
        doReturn("TestInstanceName").when(globalConfigDataForSonarInstance).getName();
        assertFalse(publisher.prebuild(abstractBuild, buildListener));
        verify(buildListener).error(anyString(), anyObject());
    }

    @Test
    public void testPrebuildShouldPassBecauseGlobalConfigIsFound() {
        doReturn(publisherDescriptor).when(jobExecutionService).getPublisherDescriptor();
        doReturn(false).when(jobExecutionService).hasGlobalConfigDataWithSameName(jobConfigData, globalConfig);
        assertTrue(publisher.prebuild(abstractBuild, buildListener));
        verifyZeroInteractions(buildListener);
    }

    @Test
    public void testPrebuildShouldPassBecauseNumberOfSonarInstancesIsZeroAndRunsWithDefault() {
        doReturn(publisherDescriptor).when(jobExecutionService).getPublisherDescriptor();
        doReturn(true).when(jobExecutionService).hasGlobalConfigDataWithSameName(jobConfigData, globalConfig);
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).getListOfGlobalConfigData();
        doReturn(0).when(globalConfigDataForSonarInstances).size();
        assertTrue(publisher.prebuild(abstractBuild, buildListener));
        verifyZeroInteractions(buildListener);
    }

    @Test
    public void testPrebuildShouldThrowException() {
        QGException exception = mock(QGException.class);
        doThrow(exception).when(jobExecutionService).getPublisherDescriptor();
        publisher.prebuild(abstractBuild, buildListener);
        verify(exception, times(1)).printStackTrace(printStream);
    }

    @Test
    public void testPerformBuildResultFail() {
        setBuildResult(Result.FAILURE);
        buildDecisionShouldBe(false);
        assertFalse(publisher.perform(abstractBuild, launcher, buildListener));
        verifyZeroInteractions(buildDecision);
    }

    @Test
    public void testPerformBuildResultSuccessWithWarningForDefaultInstance() throws QGException {
        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(true);
        doReturn(globalConfigDataForSonarInstance).when(jobConfigData).getGlobalConfigDataForSonarInstance();
        doReturn("").when(globalConfigDataForSonarInstance).getName();
        assertTrue(publisher.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(2)).getLogger();
    }

    @Test
    public void testPerformBuildResultSuccessWithNoWarning() throws QGException {
        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(true);
        doReturn(globalConfigDataForSonarInstance).when(jobConfigData).getGlobalConfigDataForSonarInstance();
        doReturn("SomeName").when(globalConfigDataForSonarInstance).getName();
        assertTrue(publisher.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(1)).getLogger();
    }

    @Test
    public void testPerformThrowsException() throws QGException {
        setBuildResult(Result.SUCCESS);
        QGException exception = mock(QGException.class);
        when(buildDecision.getStatus(jobConfigData)).thenThrow(exception);
        assertFalse(publisher.perform(abstractBuild, launcher, buildListener));
        verify(exception, times(1)).printStackTrace(printStream);
    }

    private void buildDecisionShouldBe(boolean toBeReturned) throws QGException {
        when(buildDecision.getStatus(jobConfigData)).thenReturn(toBeReturned);
    }

    private void setBuildResult(Result result) {
        when(abstractBuild.getResult()).thenReturn(result);
    }
}