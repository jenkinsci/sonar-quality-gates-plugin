package quality.gates.jenkins.plugin;

import jenkins.model.Jenkins;

import java.util.List;

public class JobExecutionService {

    public static final String DEFAULT_CONFIGURATION_WARNING = "WARNING: Quality Gates is running with default Sonar Instance.\nURL='http//localhost:9000'\nUsername='admin'\nPassword='admin'";
    public static final String GLOBAL_CONFIG_NO_LONGER_EXISTS_ERROR = "Global configuration with name '%s' no longer exists.\n";

    public QGBuilderDescriptor getBuilderDescriptor() throws QGException {
        Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            QGBuilderDescriptor desc = (QGBuilderDescriptor) instance.getDescriptor(QGBuilder.class);
            if(desc != null){
                return desc;
            }
            else
                throw new QGException("Descriptor is null. No descriptor for build step is found.");
        } else {
            throw new QGException("Jenkins instance is null. No instance is found.");
        }
    }

    public QGPublisherDescriptor getPublisherDescriptor() throws QGException {
        Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            QGPublisherDescriptor desc = (QGPublisherDescriptor) instance.getDescriptor(QGPublisher.class);
            if(desc != null){
                return desc;
            }
            else
                throw new QGException("Descriptor is null. No descriptor for post-build step is found.");
        } else {
            throw new QGException("Jenkins instance is null. No instance is found.");
        }
    }
}
