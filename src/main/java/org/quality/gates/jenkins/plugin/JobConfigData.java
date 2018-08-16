package org.quality.gates.jenkins.plugin;

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

        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        JobConfigData that = (JobConfigData) o;

        if (!projectKey.equals(that.projectKey))
            return false;

        return sonarInstanceName.equals(that.sonarInstanceName);
    }

    @Override
    public int hashCode() {

        int result = projectKey.hashCode();
        result = 31 * result + sonarInstanceName.hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "JobConfigData{" +
                "projectKey='" + projectKey + '\'' +
                ", sonarInstanceName='" + sonarInstanceName +
                '}';
    }
}
