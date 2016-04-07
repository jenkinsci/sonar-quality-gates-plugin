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

public class QGBuilderIT {

    public static final String TEST_NAME = "TestName";
    private QGBuilder qgBuilder;

    private JobConfigData jobConfigData;

    private FreeStyleProject freeStyleProject;

    private GlobalConfig globalConfig;

    @Mock
    private BuildDecision buildDecision;

    private JobExecutionService jobExecutionService;

    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;

    private List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstanceList;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        jobConfigData = new JobConfigData();
        jobExecutionService = new JobExecutionService();
        globalConfigDataForSonarInstance = new GlobalConfigDataForSonarInstance();
        qgBuilder = new QGBuilder(jobConfigData, buildDecision, jobExecutionService);
        globalConfig = GlobalConfiguration.all().get(GlobalConfig.class);
        freeStyleProject = jenkinsRule.createFreeStyleProject("freeStyleProject");
        freeStyleProject.getBuildersList().add(qgBuilder);
        globalConfigDataForSonarInstanceList = new ArrayList<>();
    }

    @Test
    public void testPrebuildShouldFailBuildNoGlobalConfigWithSameName() throws Exception{
        create2InstancesOfGlobalConfigDataAndSetTheirName(TEST_NAME,"DifferentName");
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("'TestName' no longer exists.", lastRun);
    }

    @Test
    public void testPerformShouldSucceedWithNoWarning() throws Exception {
        create2InstancesOfGlobalConfigDataAndSetTheirName(TEST_NAME, TEST_NAME);
        doReturn(true).when(buildDecision).getStatus(jobConfigData);
        jenkinsRule.buildAndAssertSuccess(freeStyleProject);
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("build passed: TRUE", lastRun);
    }

    @Test
    public void testPerformShouldSucceedWithWarning() throws Exception {
        create2InstancesOfGlobalConfigDataAndSetTheirName("","");
        doReturn(true).when(buildDecision).getStatus(jobConfigData);
        jenkinsRule.buildAndAssertSuccess(freeStyleProject);
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains(JobExecutionService.DEFAULT_CONFIGURATION_WARNING, lastRun);
        jenkinsRule.assertLogContains("build passed: TRUE", lastRun);
    }

    @Test
    public void testPerformShouldFail() throws Exception {
        create2InstancesOfGlobalConfigDataAndSetTheirName(TEST_NAME, TEST_NAME);
        doReturn(false).when(buildDecision).getStatus(jobConfigData);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("build passed: FALSE", lastRun);
    }

    @Test
    public void testPerformShouldCatchQGException() throws Exception {
        create2InstancesOfGlobalConfigDataAndSetTheirName(TEST_NAME, TEST_NAME);
        QGException exception = new QGException("TestException");
        doThrow(exception).when(buildDecision).getStatus(jobConfigData);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("QGException", lastRun);
    }

    private void create2InstancesOfGlobalConfigDataAndSetTheirName(String firstInstanceName, String secondInstanceName) {
        globalConfigDataForSonarInstance.setName(firstInstanceName);
        GlobalConfigDataForSonarInstance newInstance = new GlobalConfigDataForSonarInstance();
        newInstance.setName(secondInstanceName);
        jobConfigData.setGlobalConfigDataForSonarInstance(globalConfigDataForSonarInstance);
        globalConfigDataForSonarInstanceList.add(newInstance);
        globalConfig.setGlobalConfigDataForSonarInstances(globalConfigDataForSonarInstanceList);
    }

    private FreeStyleBuild buildProject(FreeStyleProject freeStyleProject) throws InterruptedException, ExecutionException {
        return freeStyleProject.scheduleBuild2(0).get();
    }
}
