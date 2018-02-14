package org.quality.gates.jenkins.plugin;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quality.gates.jenkins.plugin.enumeration.BuildStatusEnum;

import java.io.PrintStream;
import java.io.PrintWriter;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private JobConfigurationService jobConfigurationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        builder = new QGBuilder(jobConfigData, buildDecision, jobExecutionService, jobConfigurationService, globalConfigDataForSonarInstance);
        when(jobConfigurationService.checkProjectKeyIfVariable(any(), any(), any())).thenReturn(jobConfigData);
        when(jobConfigData.getBuildStatus()).thenReturn(BuildStatusEnum.FAILED);
        doReturn(printStream).when(buildListener).getLogger();
        doReturn(printWriter).when(buildListener).error(anyString(), anyObject());
    }

    @Test
    public void testPrebuildShouldFailGlobalConfigDataInstanceIsNull() {
        doReturn(null).when(buildDecision).chooseSonarInstance(any(GlobalConfig.class), any(JobConfigData.class));
        doReturn("TestInstanceName").when(jobConfigData).getSonarInstanceName();
        assertFalse(builder.prebuild(abstractBuild, buildListener));
        verify(buildListener).error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, "TestInstanceName");
    }

    @Test
    public void testPerformShouldFailWithNoWarning() throws QGException {
        String stringWithName = "Name";
        when(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData, buildListener)).thenReturn(false);
        when(jobConfigData.getSonarInstanceName()).thenReturn(stringWithName);
        assertFalse(builder.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    @Test
    public void testPerformShouldFailWithWarning() throws QGException {
        String emptyString = "";
        when(buildDecision.getStatus(globalConfigDataForSonarInstance, jobConfigData, buildListener)).thenReturn(false);
        when(jobConfigData.getSonarInstanceName()).thenReturn(emptyString);
        assertFalse(builder.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }
}
