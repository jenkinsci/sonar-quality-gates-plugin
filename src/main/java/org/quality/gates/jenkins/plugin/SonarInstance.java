package org.quality.gates.jenkins.plugin;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class SonarInstance extends AbstractDescribableImpl<SonarInstance> {

    public static final String DEFAULT_URL = "http://localhost:9000";

    public static final String DEFAULT_USERNAME = "admin";

    public static final String DEFAULT_PASS = "admin";

    private String name;

    private String username;

    private String pass;

    private String sonarUrl;

    private Secret secretPass;

    private Secret token;

    private int timeToWait;

    private int maxWaitTime;

    public SonarInstance() {
        this.name = "";
        this.sonarUrl = "";
        this.username = "";
        this.pass = "";
    }

    @DataBoundConstructor
    public SonarInstance(
            String name, String sonarUrl, String username, Secret secretPass, int timeToWait, int maxWaitTime) {
        this.name = name;
        this.sonarUrl = sonarUrl;
        this.username = username;
        this.secretPass = secretPass;
        this.timeToWait = timeToWait;
        this.maxWaitTime = maxWaitTime;
    }

    public SonarInstance(String name, String sonarUrl, String username, String pass) {
        this.name = name;
        this.sonarUrl = sonarUrl;
        this.username = username;
        this.pass = pass;
    }

    public SonarInstance(String name, String sonarUrl, Secret token, int timeToWait, int maxWaitTime) {
        this.name = name;
        this.sonarUrl = sonarUrl;
        this.token = token;
        this.timeToWait = timeToWait;
        this.maxWaitTime = maxWaitTime;
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = username;
    }

    public Secret getPass() {
        return secretPass != null ? secretPass : Secret.fromString("");
    }

    @DataBoundSetter
    public void setPass(String pass) {
        this.secretPass = Secret.fromString(Util.fixEmptyAndTrim(pass));
    }

    public String getSonarUrl() {
        return sonarUrl;
    }

    @DataBoundSetter
    public void setSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl;
    }

    public int getTimeToWait() {
        return timeToWait;
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    @DataBoundSetter
    public void setTimeToWait(int timeToWait) {
        this.timeToWait = timeToWait;
    }

    @DataBoundSetter
    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public Secret getToken() {
        return token != null ? token : Secret.fromString("");
    }

    @DataBoundSetter
    public void setToken(String token) {
        this.token = Secret.fromString(Util.fixEmptyAndTrim(token));
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SonarInstance> {}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        var that = (SonarInstance) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }

        if (token != null ? !token.equals(that.token) : that.token != null) {
            return false;
        }

        if (pass != null ? !pass.equals(that.pass) : that.pass != null) {
            return false;
        }

        return sonarUrl != null ? sonarUrl.equals(that.sonarUrl) : that.sonarUrl == null;
    }

    @Override
    public int hashCode() {
        var result = name != null ? name.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (pass != null ? pass.hashCode() : 0);
        result = 31 * result + (sonarUrl != null ? sonarUrl.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "GlobalConfigDataForSonarInstance{" + "name='"
                + name + '\'' + ", username='"
                + username + '\'' + ", sonarUrl='"
                + sonarUrl + '\'' + ", timeToWait='"
                + timeToWait + '\'' + ", maxWaitTime="
                + maxWaitTime + '}';
    }
}
