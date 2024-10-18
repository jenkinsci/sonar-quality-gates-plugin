package org.quality.gates.jenkins.plugin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import jenkins.model.GlobalConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class QGBuilderIT {

    public static final String TEST_NAME = "TestName";

    private QGBuilder qgBuilder;

    private JobConfigData jobConfigData;

    private FreeStyleProject freeStyleProject;

    private GlobalSonarQualityGatesConfiguration globalConfig;

    @Mock
    private BuildDecision buildDecision;

    @Mock
    private BuildListener listener;

    private JobConfigurationService jobConfigurationService;

    private JobExecutionService jobExecutionService;

    private SonarInstance sonarInstance;

    private List<SonarInstance> globalConfigDataForSonarInstanceList;

    private QGBuilderDescriptor builderDescriptor;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private AutoCloseable closeable;

    @Before
    public void setUp() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);
        jobConfigData = new JobConfigData();
        jobConfigurationService = new JobConfigurationService();
        jobExecutionService = new JobExecutionService();
        sonarInstance = new SonarInstance();
        builderDescriptor = new QGBuilderDescriptor(jobExecutionService, jobConfigurationService);
        qgBuilder = new QGBuilder(
                jobConfigData, buildDecision, jobExecutionService, jobConfigurationService, sonarInstance);
        globalConfig = GlobalConfiguration.all().get(GlobalSonarQualityGatesConfiguration.class);
        freeStyleProject = jenkinsRule.createFreeStyleProject("freeStyleProject");
        freeStyleProject.getBuildersList().add(qgBuilder);
        globalConfigDataForSonarInstanceList = new ArrayList<>();
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testPrebuildShouldFailBuildNoGlobalConfigWithSameName() throws Exception {
        doReturn(null).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData);
        jobConfigData.setSonarInstanceName(TEST_NAME);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("'TestName' no longer exists.", lastRun);
    }

    @Test
    public void testPerformShouldSucceedWithNoWarning() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames(TEST_NAME, TEST_NAME);
        jobConfigData.setProjectKey("projectKey");
        doReturn(sonarInstance).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData);
        doReturn(true)
                .when(buildDecision)
                .getStatus(any(SonarInstance.class), any(JobConfigData.class), any(BuildListener.class));
        jenkinsRule.buildAndAssertSuccess(freeStyleProject);
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("build passed: TRUE", lastRun);
    }

    @Test
    public void testPerformShouldSucceedWithWarning() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames("", "");
        jobConfigData.setProjectKey("projectKey");
        doReturn(sonarInstance).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData);
        doReturn(true)
                .when(buildDecision)
                .getStatus(any(SonarInstance.class), any(JobConfigData.class), any(BuildListener.class));
        jenkinsRule.buildAndAssertSuccess(freeStyleProject);
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains(JobExecutionService.DEFAULT_CONFIGURATION_WARNING, lastRun);
        jenkinsRule.assertLogContains("build passed: TRUE", lastRun);
    }

    @Test
    public void testPerformShouldFail() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames(TEST_NAME, TEST_NAME);
        jobConfigData.setProjectKey("projectKey");
        doReturn(sonarInstance).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData);
        doReturn(false)
                .when(buildDecision)
                .getStatus(any(SonarInstance.class), any(JobConfigData.class), any(BuildListener.class));
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("build passed: FALSE", lastRun);
    }

    @Test
    public void testPerformShouldCatchQGException() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames(TEST_NAME, TEST_NAME);
        jobConfigData.setProjectKey("projectKey");
        QGException exception = new QGException("TestException");
        doReturn(sonarInstance).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData);
        doThrow(exception)
                .when(buildDecision)
                .getStatus(any(SonarInstance.class), any(JobConfigData.class), any(BuildListener.class));
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("QGException", lastRun);
    }

    private void setGlobalConfigDataAndJobConfigDataNames(String firstInstanceName, String secondInstanceName) {
        sonarInstance.setName(firstInstanceName);
        jobConfigData.setSonarInstanceName(secondInstanceName);
        globalConfig.setSonarInstances(globalConfigDataForSonarInstanceList);
    }

    private FreeStyleBuild buildProject(FreeStyleProject freeStyleProject)
            throws InterruptedException, ExecutionException {
        return freeStyleProject.scheduleBuild2(0).get();
    }
}
