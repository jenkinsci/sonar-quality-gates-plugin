package quality.gates.jenkins.plugin;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QGBuilderTest {

    private QGBuilder builder;

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

    private QGBuilderDescriptor builderDescriptor;

    @Mock
    JobConfigurationService jobConfigurationService;

    @Mock
    List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstances;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        builder = new QGBuilder(jobConfigData, buildDecision, jobExecutionService);
        builderDescriptor = new QGBuilderDescriptor(globalConfig, jobConfigurationService);
        doReturn(printStream).when(buildListener).getLogger();
        doReturn(printWriter).when(buildListener).error(anyString(), anyObject());
    }

    @Test
    public void testPrebuildShouldFail() {
        doReturn(builderDescriptor).when(jobExecutionService).getBuilderDescriptor();
        hasGlobalConfigurationWithSameName(false);
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).getListOfGlobalConfigData();
        doReturn(1).when(globalConfigDataForSonarInstances).size();
        doReturn(globalConfigDataForSonarInstance).when(jobConfigData).getGlobalConfigDataForSonarInstance();
        doReturn("TestInstanceName").when(globalConfigDataForSonarInstance).getName();
        assertFalse(builder.prebuild(abstractBuild, buildListener));
        verify(buildListener).error(anyString(), anyObject());
    }

    @Test
    public void testPrebuildShouldPassBecauseGlobalConfigIsFound() {
        doReturn(builderDescriptor).when(jobExecutionService).getBuilderDescriptor();
        hasGlobalConfigurationWithSameName(false);
        assertTrue(builder.prebuild(abstractBuild, buildListener));
        verifyZeroInteractions(buildListener);
    }

    @Test
    public void testPrebuildShouldPassBecauseNumberOfSonarInstancesIsZeroAndRunsWithDefault() {
        doReturn(builderDescriptor).when(jobExecutionService).getBuilderDescriptor();
        hasGlobalConfigurationWithSameName(true);
        doReturn(globalConfigDataForSonarInstances).when(globalConfig).getListOfGlobalConfigData();
        doReturn(0).when(globalConfigDataForSonarInstances).size();
        assertTrue(builder.prebuild(abstractBuild, buildListener));
        verifyZeroInteractions(buildListener);
    }

    @Test
    public void testPrebuildShouldThrowException() {
        QGException exception = mock(QGException.class);
        doThrow(exception).when(jobExecutionService).getBuilderDescriptor();
        builder.prebuild(abstractBuild, buildListener);
        verify(exception, times(1)).printStackTrace(printStream);
    }

    private void hasGlobalConfigurationWithSameName(boolean doReturn) {
        doReturn(doReturn).when(jobExecutionService).hasGlobalConfigDataWithSameName(jobConfigData, globalConfig);
    }

    @Test
    public void testPerformShouldPass() throws QGException {
        String stringWithName = "Name";
        when(buildDecision.getStatus(jobConfigData)).thenReturn(true);
        when(jobConfigData.getGlobalConfigDataForSonarInstance()).thenReturn(globalConfigDataForSonarInstance);
        when(globalConfigDataForSonarInstance.getName()).thenReturn(stringWithName);
        assertTrue(builder.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(1)).getLogger();
    }

    @Test
    public void testPerformShouldFail() throws QGException {
        String emptyString = "";
        when(buildDecision.getStatus(jobConfigData)).thenReturn(false);
        when(jobConfigData.getGlobalConfigDataForSonarInstance()).thenReturn(globalConfigDataForSonarInstance);
        when(globalConfigDataForSonarInstance.getName()).thenReturn(emptyString);
        assertFalse(builder.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(2)).getLogger();
    }

    @Test
    public void testPerformThrowsException() throws QGException {
        QGException exception = mock(QGException.class);
        when(buildDecision.getStatus(jobConfigData)).thenThrow(exception);
        assertFalse(builder.perform(abstractBuild, launcher, buildListener));
        verify(exception, times(1)).printStackTrace(printStream);
    }
}