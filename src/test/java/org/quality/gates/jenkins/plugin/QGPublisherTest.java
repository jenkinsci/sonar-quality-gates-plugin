package org.quality.gates.jenkins.plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quality.gates.jenkins.plugin.enumeration.BuildStatusEnum;

public class QGPublisherTest {

    public static final String POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED =
            "PostBuild-Step: Quality Gates plugin build passed: ";

    @InjectMocks
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
    private AbstractBuild<?, ?> abstractBuild;

    @Mock
    private Launcher launcher;

    @Mock
    private SonarInstance sonarInstance;

    @Mock
    private JobConfigurationService jobConfigurationService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        publisher = new QGPublisher(
                jobConfigData, buildDecision, jobExecutionService, jobConfigurationService, sonarInstance);
        when(jobConfigurationService.checkProjectKeyIfVariable(any(), any(), any()))
                .thenReturn(jobConfigData);
        when(jobConfigData.getBuildStatus()).thenReturn(BuildStatusEnum.FAILED);
        doReturn(printStream).when(buildListener).getLogger();
        doReturn(printWriter).when(buildListener).error(anyString(), any());
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testPrebuildShouldFail() {
        doReturn(null)
                .when(buildDecision)
                .chooseSonarInstance(any(GlobalSonarQualityGatesConfiguration.class), any(JobConfigData.class));
        doReturn("TestInstanceName").when(jobConfigData).getSonarInstanceName();
        assertFalse(publisher.prebuild(abstractBuild, buildListener));
        verify(buildListener).error(JobExecutionService.GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR, "TestInstanceName");
    }

    @Test
    void testPerformBuildResultFail() {
        setBuildResult(Result.FAILURE);
        buildDecisionShouldBe(false);
        assertFalse(publisher.perform(abstractBuild, launcher, buildListener));
        verifyNoInteractions(buildDecision);
    }

    @Test
    void testPerformBuildResultFailWithWarningForDefaultInstance() throws QGException {
        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(false);
        when(jobConfigData.getSonarInstanceName()).thenReturn("");
        assertFalse(publisher.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(2)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(JobExecutionService.DEFAULT_CONFIGURATION_WARNING);
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    @Test
    void testPerformBuildResultFailWithNoWarning() throws QGException {
        setBuildResult(Result.SUCCESS);
        buildDecisionShouldBe(false);
        doReturn("SomeName").when(sonarInstance).getName();
        assertFalse(publisher.perform(abstractBuild, launcher, buildListener));
        verify(buildListener, times(1)).getLogger();
        PrintStream stream = buildListener.getLogger();
        verify(stream).println(POST_BUILD_STEP_QUALITY_GATES_PLUGIN_BUILD_PASSED + "FALSE");
    }

    private void buildDecisionShouldBe(boolean toBeReturned) throws QGException {
        when(buildDecision.getStatus(sonarInstance, jobConfigData, buildListener))
                .thenReturn(toBeReturned);
    }

    private void setBuildResult(Result result) {
        when(abstractBuild.getResult()).thenReturn(result);
    }
}
