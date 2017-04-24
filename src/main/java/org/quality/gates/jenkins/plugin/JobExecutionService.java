package org.quality.gates.jenkins.plugin;

import jenkins.model.GlobalConfiguration;

public class JobExecutionService {

    public static final String DEFAULT_CONFIGURATION_WARNING = "WARNING: Quality Gates is running with default Sonar Instance.\nURL='http//localhost:9000'\nUsername='admin'\nPassword='admin'";

    public static final String GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR = "The Sonar Instance in the global configuration with name '%s' no longer exists.\n";

    public GlobalConfig getGlobalConfigData() {

        GlobalConfig globalConfig = GlobalConfiguration.all().get(GlobalConfig.class);

        if (globalConfig == null) {
            return new GlobalConfig();
        }

        return globalConfig;
    }
}
