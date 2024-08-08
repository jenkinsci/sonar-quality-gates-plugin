package org.quality.gates.jenkins.plugin;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Objects;

public class GlobalConfigDataForSonarInstance {

    public static final String DEFAULT_URL = "http://localhost:9000";

    private String name;

    private String sonarUrl;

    private String sonarCredentialsId;

    private int timeToWait;

    private int maxWaitTime;

    public GlobalConfigDataForSonarInstance() {
        this.name = "";
        this.sonarUrl = "";
        this.sonarCredentialsId = "";
    }

    public GlobalConfigDataForSonarInstance(
            String name, String sonarUrl, String sonarCredentialsId, int timeToWait, int maxWaitTime) {
        this.name = name;
        this.sonarUrl = isEmpty(sonarUrl) ? DEFAULT_URL : sonarUrl;
        this.sonarCredentialsId = sonarCredentialsId;
        this.timeToWait = timeToWait;
        this.maxWaitTime = maxWaitTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSonarUrl() {
        return sonarUrl;
    }

    public void setSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl;
    }

    public int getTimeToWait() {
        return timeToWait;
    }

    public void setTimeToWait(int timeToWait) {
        this.timeToWait = timeToWait;
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public String getSonarCredentialsId() {
        return sonarCredentialsId;
    }

    public void setSonarCredentialsId(String sonarCredentialsId) {
        this.sonarCredentialsId = sonarCredentialsId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalConfigDataForSonarInstance that = (GlobalConfigDataForSonarInstance) o;
        return timeToWait == that.timeToWait
                && maxWaitTime == that.maxWaitTime
                && Objects.equals(name, that.name)
                && Objects.equals(sonarUrl, that.sonarUrl)
                && Objects.equals(sonarCredentialsId, that.sonarCredentialsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sonarUrl, sonarCredentialsId, timeToWait, maxWaitTime);
    }

    @Override
    public String toString() {
        return "GlobalConfigDataForSonarInstance{" + "name='"
                + name + '\'' + ", sonarUrl='"
                + sonarUrl + '\'' + ", sonarCredentialsId='"
                + sonarCredentialsId + '\'' + ", timeToWait="
                + timeToWait + ", maxWaitTime="
                + maxWaitTime + '}';
    }
}
