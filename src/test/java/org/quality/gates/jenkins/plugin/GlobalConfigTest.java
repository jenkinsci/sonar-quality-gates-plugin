package org.quality.gates.jenkins.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import hudson.model.Descriptor;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class GlobalConfigTest {

    private GlobalSonarQualityGatesConfiguration globalConfig;

    @InjectMocks
    private GlobalSonarQualityGatesConfiguration spyGlobalConfig;

    @Mock
    private StaplerRequest staplerRequest;

    @Mock
    private GlobalConfigurationService globalConfigurationService;

    private List<SonarInstance> globalConfigDataForSonarInstances;

    private JSONObject jsonObject;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        jsonObject = new JSONObject();
        globalConfigDataForSonarInstances = new ArrayList<>();
        globalConfig =
                new GlobalSonarQualityGatesConfiguration(globalConfigDataForSonarInstances, globalConfigurationService);
        spyGlobalConfig = spy(globalConfig);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testConfigure() throws Descriptor.FormException {
        doNothing().when(spyGlobalConfig).save();
        doReturn(globalConfigDataForSonarInstances)
                .when(globalConfigurationService)
                .instantiateGlobalConfigData(any(JSONObject.class));
        assertTrue(spyGlobalConfig.configure(staplerRequest, jsonObject));
    }

    @Test
    public void testGetSonarInstanceByNameIF() {
        SonarInstance globalConfig1 = new SonarInstance();
        globalConfig1.setName("Name");
        globalConfigDataForSonarInstances.add(globalConfig1);

        String name = "Name";
        assertEquals(name, globalConfig.getSonarInstanceByName(name).getName());
    }

    @Test
    public void testGetSonarInstanceByNameELSE() {
        SonarInstance globalConfig1 = new SonarInstance();
        globalConfig1.setName("Name");
        globalConfigDataForSonarInstances.add(globalConfig1);

        assertNull(globalConfig.getSonarInstanceByName(""));
    }

    @Test
    public void testGetSonarInstanceByNameNULL() {
        assertNull(globalConfig.getSonarInstanceByName(""));
    }
}
