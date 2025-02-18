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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@WithJenkins
class QGPublisherIT {

    private static final String TEST_NAME = "TestName";

    private QGPublisher publisher;

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

    private JenkinsRule jenkinsRule;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp(JenkinsRule jenkinsRule) throws IOException {
        this.jenkinsRule = jenkinsRule;
        closeable = MockitoAnnotations.openMocks(this);
        jobConfigData = new JobConfigData();
        jobExecutionService = new JobExecutionService();
        sonarInstance = new SonarInstance();
        publisher = new QGPublisher(
                jobConfigData, buildDecision, jobExecutionService, jobConfigurationService, sonarInstance);
        globalConfig = GlobalConfiguration.all().get(GlobalSonarQualityGatesConfiguration.class);
        freeStyleProject = jenkinsRule.createFreeStyleProject("freeStyleProject");
        freeStyleProject.getPublishersList().add(publisher);
        globalConfigDataForSonarInstanceList = new ArrayList<>();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testPrebuildShouldFailBuildNoGlobalConfigWithSameName() throws Exception {
        jobConfigData.setSonarInstanceName(TEST_NAME);
        doReturn(null).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("'TestName' no longer exists.", lastRun);
    }

    @Test
    void testPerformShouldFailBecauseOfPreviousSteps() throws Exception {
        setGlobalConfigDataAndJobConfigDataNames(TEST_NAME, TEST_NAME);
        doReturn(null).when(buildDecision).chooseSonarInstance(globalConfig, jobConfigData);
        doReturn(false).when(buildDecision).getStatus(sonarInstance, jobConfigData, listener);
        jenkinsRule.assertBuildStatus(Result.FAILURE, buildProject(freeStyleProject));
        Run lastRun = freeStyleProject._getRuns().newestValue();
        jenkinsRule.assertLogContains("Previous steps failed the build", lastRun);
    }

    @Test
    void testPerformShouldFail() throws Exception {
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
