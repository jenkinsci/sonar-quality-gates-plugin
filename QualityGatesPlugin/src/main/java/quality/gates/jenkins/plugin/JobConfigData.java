package quality.gates.jenkins.plugin;

public class JobConfigData {

    private String projectKey;
    private GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance;


    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public GlobalConfigDataForSonarInstance getGlobalConfigDataForSonarInstance() {
        return globalConfigDataForSonarInstance;
    }

    public void setGlobalConfigDataForSonarInstance(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {
        this.globalConfigDataForSonarInstance = globalConfigDataForSonarInstance;
    }

    @Override
    public String toString() {
        return "JobConfigData{" +
                "projectKey='" + projectKey + '\'' +
                ", globalConfigDataForSonarInstance=" + globalConfigDataForSonarInstance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobConfigData that = (JobConfigData) o;

        if (projectKey != null ? !projectKey.equals(that.projectKey) : that.projectKey != null) return false;
        return globalConfigDataForSonarInstance != null ? globalConfigDataForSonarInstance.equals(that.globalConfigDataForSonarInstance) : that.globalConfigDataForSonarInstance == null;

    }

    @Override
    public int hashCode() {
        int result = projectKey != null ? projectKey.hashCode() : 0;
        result = 31 * result + (globalConfigDataForSonarInstance != null ? globalConfigDataForSonarInstance.hashCode() : 0);
        return result;
    }
}
