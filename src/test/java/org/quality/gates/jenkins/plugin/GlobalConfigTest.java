package org.quality.gates.jenkins.plugin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import hudson.model.Descriptor;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.StaplerRequest2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class GlobalConfigTest {

    private GlobalSonarQualityGatesConfiguration globalConfig;

    @InjectMocks
    private GlobalSonarQualityGatesConfiguration spyGlobalConfig;

    @Mock
    private StaplerRequest2 staplerRequest;

    @Mock
    private GlobalConfigurationService globalConfigurationService;

    private List<SonarInstance> globalConfigDataForSonarInstances;

    private JSONObject jsonObject;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        jsonObject = new JSONObject();
        globalConfigDataForSonarInstances = new ArrayList<>();
        globalConfig =
                new GlobalSonarQualityGatesConfiguration(globalConfigDataForSonarInstances, globalConfigurationService);
        spyGlobalConfig = spy(globalConfig);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testConfigure() throws Descriptor.FormException {
        doNothing().when(spyGlobalConfig).save();
        doReturn(globalConfigDataForSonarInstances)
                .when(globalConfigurationService)
                .instantiateGlobalConfigData(any(JSONObject.class));
        assertTrue(spyGlobalConfig.configure(staplerRequest, jsonObject));
    }

    @Test
    void testGetSonarInstanceByNameIF() {
        SonarInstance globalConfig1 = new SonarInstance();
        globalConfig1.setName("Name");
        globalConfigDataForSonarInstances.add(globalConfig1);

        String name = "Name";
        assertEquals(name, globalConfig.getSonarInstanceByName(name).getName());
    }

    @Test
    void testGetSonarInstanceByNameELSE() {
        SonarInstance globalConfig1 = new SonarInstance();
        globalConfig1.setName("Name");
        globalConfigDataForSonarInstances.add(globalConfig1);

        assertNull(globalConfig.getSonarInstanceByName(""));
    }

    @Test
    void testGetSonarInstanceByNameNULL() {
        assertNull(globalConfig.getSonarInstanceByName(""));
    }
}
