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

    public static final String BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED = "Build-Step: Quality Gates plugin build passed: ";
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
        builderDescriptor = new QGBuilderDescriptor(globalConfig, jobConfigurationService);
        builder = new QGBuilder(jobConfigData, buildDecision, jobExecutionService, builderDescriptor, globalConfigDataForSonarInstance);
        doReturn(printStream).when(buildListener).getLogger();
        doReturn(printWriter).when(buildListener).error(anyString(), anyObject());
    }

    @Test
    public void testPrebuildShouldFailGlobalConfigDataInstanceIsNull() {
        doReturn(builderDescriptor).when(jobExecutionService).getBuilderDescriptor();
        doReturn(null).when(buildDecision).chooseSonarInstance(any(GlobalConfig.class), anyString());
        doReturn("TestInstanceName").when(jobConfigData).getSonarInstanceName();
        assertFalse(builder.prebuild(abstractBuild, buildListener));
        verify(buildListener).error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, "TestInstanceName");
    }

    @Test
    public void testPrebuildShouldPassBecauseGlobalConfigDataIsFound() {
        doReturn(builderDescriptor).when(jobExecutionService).getBuilderDescriptor();
        doReturn(globalConfigDataForSonarInstance).when(buildDecision).chooseSonarInstance(any(GlobalConfig.class), anyString());
        assertTrue(builder.prebuild(abstractBuild, buildListener));
        verifyZeroInteractions(buildListener);
    }

    @Test
    public void testPerformShouldPassWithNoWarning() throws QGException {
        String stringWithName = "Name";
        when(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData)).thenReturn(true);
        when(jobConfigData.getSonarInstanceName()).thenReturn(stringWithName);
        assertTrue(builder.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformShouldPassWithWarning() throws QGException {
        String emptyString = "";
        when(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData)).thenReturn(true);
        when(jobConfigData.getSonarInstanceName()).thenReturn(emptyString);
        assertTrue(builder.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "TRUE");
    }

    @Test
    public void testPerformShouldFailWithNoWarning() throws QGException {
        String stringWithName = "Name";
        when(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData)).thenReturn(false);
        when(jobConfigData.getSonarInstanceName()).thenReturn(stringWithName);
        assertFalse(builder.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    @Test
    public void testPerformShouldFailWithWarning() throws QGException {
        String emptyString = "";
        when(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData)).thenReturn(false);
        when(jobConfigData.getSonarInstanceName()).thenReturn(emptyString);
        assertFalse(builder.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    @Test
    public void testPerformThrowsException() throws QGException {
        QGException exception = mock(QGException.class);
        when(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData)).thenThrow(exception);
        assertFalse(builder.perform(abstractBuild, launcher, buildListener));
        verify(exception, times(1)).printStackTrace(printStream);
    }
}