package org.quality.gates.jenkins.plugin;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

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

public class QGPublisherIT {

    public static final String TEST_NAME = "TestName";

    private QGPublisher publisher;

    private QGBuilder builder;

    private JobConfigData jobConfigData;

    private FreeStyleProject freeStyleProject;

    private GlobalSonarQualityGatesConfiguration globalConfig;

    @Mock
    private BuildDecision buildDecision;

    @Mock
    private BuildListener listener;

    @Mock
    private JobConfigurationService jobConfigurationService;

    private JobExecutionService jobExecutionService;

    private SonarInstance sonarInstance;

    private List<SonarInstance> globalConfigDataForSonarInstanceList;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private AutoCloseable closeable;

    @Before
    public void setUp() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);
        jobConfigData = new JobConfigData();
        jobExecutionService = new JobExecutionService();
        sonarInstance = new SonarInstance();
        publisher = new QGPublisher(
                jobConfigData, buildDecision, jobExecutionService, jobConfigurationService, sonarInstance);
        builder = new QGBuilder(
                jobConfigData, buildDecision, jobExecutionService, jobConfigurationService, sonarInstance);
        globalConfig = GlobalConfiguration.all().get(GlobalSonarQualityGatesConfiguration.class);
        freeStyleProject = jenkinsRule.createFreeStyleProject("freeStyleProject");
        freeStyleProject.getBuildersList().add(builder);
        freeStyleProject.getPublishersList().add(publisher);
        globalConfigDataForSonarInstanceList = new ArrayList<>();
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testPrebuildShouldFailBuildNoGlobalConfigWithSameName() throws Exception {
        jobConfigData.setSonarInstanceName(TEST_NAME);
        doReturn(null).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("'TestName' no longer exists.", lastRun);
    }

    @Test
    public void testPerformShouldFailBecauseOfPreviousSteps() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames(TEST_NAME, TEST_NAME);
        doReturn(null).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData);
        doReturn(false).when(buildDecision).getStatus(sonarInstance, jobConfigData, listener);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("Previous steps failed the build", lastRun);
    }

    @Test
    public void testPerformShouldFail() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames(TEST_NAME, TEST_NAME);
        doReturn(sonarInstance).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData);
        when(buildDecision.getStatus(sonarInstance, jobConfigData, listener)).thenReturn(true, false);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("build passed: FALSE", lastRun);
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
