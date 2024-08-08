package org.quality.gates.jenkins.plugin;

import java.util.Objects;
import org.quality.gates.jenkins.plugin.enumeration.BuildStatusEnum;

public class JobConfigData {

    private String projectKey;

    private String sonarInstanceName;

    private BuildStatusEnum buildStatus;

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getSonarInstanceName() {
        return sonarInstanceName;
    }

    public void setSonarInstanceName(String sonarInstanceName) {
        this.sonarInstanceName = sonarInstanceName;
    }

    public BuildStatusEnum getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(BuildStatusEnum buildStatus) {
        this.buildStatus = buildStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobConfigData that = (JobConfigData) o;
        return Objects.equals(projectKey, that.projectKey)
                && Objects.equals(sonarInstanceName, that.sonarInstanceName)
                && buildStatus == that.buildStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectKey, sonarInstanceName, buildStatus);
    }

    @Override
    public String toString() {
        return "JobConfigData{" + "projectKey='"
                + projectKey + '\'' + ", sonarInstanceName='"
                + sonarInstanceName + '\'' + ", buildStatus="
                + buildStatus + '}';
    }
}
