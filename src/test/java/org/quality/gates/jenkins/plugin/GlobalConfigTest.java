package org.quality.gates.jenkins.plugin;

import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quality.gates.jenkins.plugin.GlobalConfig;
import org.quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import org.quality.gates.jenkins.plugin.GlobalConfigurationService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class GlobalConfigTest {

    private GlobalConfig globalConfig;

    @InjectMocks
    private GlobalConfig spyGlobalConfig;

    @Mock
    private StaplerRequest staplerRequest;

    @Mock
    private GlobalConfigurationService globalConfigurationService;

    private List<GlobalConfigDataForSonarInstance> globalConfigDataForSonarInstances;

    private JSONObject jsonObject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jsonObject = new JSONObject();
        globalConfigDataForSonarInstances = new ArrayList<>();
        globalConfig = new GlobalConfig(globalConfigDataForSonarInstances, globalConfigurationService);
        spyGlobalConfig = spy(globalConfig);
    }

    @Test
    public void testConfigure() throws Descriptor.FormException {
        doNothing().when(spyGlobalConfig).save();
        doReturn(globalConfigDataForSonarInstances).when(globalConfigurationService).instantiateGlobalConfigData(any(JSONObject.class));
        assertTrue(spyGlobalConfig.configure(staplerRequest, jsonObject));
    }

    @Test
    public void testGetSonarInstanceByNameIF() {

        GlobalConfigDataForSonarInstance globalConfig1 = new GlobalConfigDataForSonarInstance();
        globalConfig1.setName("Name");
        globalConfigDataForSonarInstances.add(globalConfig1);

        String name = "Name";
        assertEquals(name, globalConfig.getSonarInstanceByName(name).getName());
    }

    @Test
    public void testGetSonarInstanceByNameELSE() {
        GlobalConfigDataForSonarInstance globalConfig1 = new GlobalConfigDataForSonarInstance();
        globalConfig1.setName("Name");
        globalConfigDataForSonarInstances.add(globalConfig1);

        assertNull(globalConfig.getSonarInstanceByName(""));
    }

    @Test
    public void testGetSonarInstanceByNameNULL() {
        assertNull(globalConfig.getSonarInstanceByName(""));
    }
}
