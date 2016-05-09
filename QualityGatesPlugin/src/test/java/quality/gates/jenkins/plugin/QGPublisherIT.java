package quality.gates.jenkins.plugin;

import hudson.model.*;
import jenkins.model.GlobalConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

public class QGPublisherIT {

    public static final String TEST_NAME = "TestName";
    private QGPublisher publisher;

    private QGBuilder builder;

    private JobConfigData jobConfigData;

    private FreeStyleProject freeStyleProject;

    private GlobalConfig globalConfig;

    @Mock
    private BuildDecision buildDecision;

    @Mock
    JobConfigurationService jobConfigurationService;

    private JobExecutionService jobExecutionService;

    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    private List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstanceList;

    private QGBuilderDescriptor builderDescriptor;

    private QGPublisherDescriptor publisherDescriptor;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        jobConfigData = new JobConfigData();
        jobExecutionService = new JobExecutionService();
        globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance();
        builderDescriptor = new QGBuilderDescriptor(globalConfig, jobConfigurationService);
        publisherDescriptor = new QGPublisherDescriptor(globalConfig, jobConfigurationService);
        publisher = new QGPublisher(jobConfigData, buildDecision, jobExecutionService, publisherDescriptor, globalConfigDataForSonarInstance);
        builder = new QGBuilder(jobConfigData, buildDecision, jobExecutionService, builderDescriptor, globalConfigDataForSonarInstance);
        globalConfig = GlobalConfiguration.all().get(GlobalConfig.class);
        freeStyleProject = jenkinsRule.createFreeStyleProject("freeStyleProject");
        freeStyleProject.getBuildersList().add(builder);
        freeStyleProject.getPublishersList().add(publisher);
        globalConfigDataForSonarInstanceList = new ArrayList<>();
    }

    @Test
    public void testPrebuildShouldFailBuildNoGlobalConfigWithSameName() throws Exception {
        jobConfigData.setSonarInstanceName(TEST_NAME);
        doReturn(null).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData.getSonarInstanceName());
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("'TestName' no longer exists.", lastRun);
    }

    @Test
    public void testPerformShouldFailBecauseOfPreviousSteps() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames(TEST_NAME, TEST_NAME);
        doReturn(null).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData.getSonarInstanceName());
        doReturn(false).when(buildDecision).getStatus(globalConfigDataForSonarInstance, jobConfigData);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("Previous steps failed the build", lastRun);
    }

    @Test
    public void testPerformShouldSucceedWithNoWarning() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames(TEST_NAME, TEST_NAME);
        doReturn(globalConfigDataForSonarInstance).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData.getSonarInstanceName());
        when(buildDecision.getStatus(globalConfigDataForSonarInstance,jobConfigData)).thenReturn(true, true);
        jenkinsRule.buildAndAssertSuccess(freeStyleProject);
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("build passed: TRUE", lastRun);
    }

    @Test
    public void testPerformShouldSucceedWithWarning() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames("","");
        doReturn(globalConfigDataForSonarInstance).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData.getSonarInstanceName());
        when(buildDecision.getStatus(globalConfigDataForSonarInstance,jobConfigData)).thenReturn(true, true);
        jenkinsRule.buildAndAssertSuccess(freeStyleProject);
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains(JobExecutionService.DEFAULT_CONFIGURATION_WARNING, lastRun);
        jenkinsRule.assertLogContains("build passed: TRUE", lastRun);
    }

    @Test
    public void testPerformShouldFail() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames(TEST_NAME, TEST_NAME);
        doReturn(globalConfigDataForSonarInstance).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData.getSonarInstanceName());
        when(buildDecision.getStatus(globalConfigDataForSonarInstance,jobConfigData)).thenReturn(true, false);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("build passed: FALSE", lastRun);
    }

    @Test
    public void testPerformShouldCatchQGException() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames(TEST_NAME, TEST_NAME);
        QGException exception = new QGException("TestException");
        doReturn(globalConfigDataForSonarInstance).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData.getSonarInstanceName());
        doReturn(true).doThrow(exception).when(buildDecision).getStatus(globalConfigDataForSonarInstance, jobConfigData);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("QGException", lastRun);
    }

    private void setGlobalConfigDataAndJobConfigDataNames(String firstInstanceName, String secondInstanceName) {
        globalConfigDataForSonarInstance.setName(firstInstanceName);
        jobConfigData.setSonarInstanceName(secondInstanceName);
        globalConfig.setGlobalConfigDataForSonarInstances(globalConfigDataForSonarInstanceList);
    }

    private FreeStyleBuild buildProject(FreeStyleProject freeStyleProject) throws InterruptedException, ExecutionException {
        return freeStyleProject.scheduleBuild2(0).get();
    }
}
