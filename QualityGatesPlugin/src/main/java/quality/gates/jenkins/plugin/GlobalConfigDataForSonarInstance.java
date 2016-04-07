package quality.gates.jenkins.plugin;

public class GlobalConfigDataForSonarInstance {

    public static final String DEFAULT_URL = "http://localhost:9000";
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PASS = "admin";

    private String name;
    private String username;
    private String pass;
    private String sonarUrl;

    public GlobalConfigDataForSonarInstance() {
        this.name = "";
        this.sonarUrl = "";
        this.username = "";
        this.pass = "";
    }

    public GlobalConfigDataForSonarInstance(String name, String sonarUrl, String username, String pass) {
        this.name = name;
        this.sonarUrl = sonarUrl;
        this.username = username;
        this.pass = pass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getSonarUrl() {
        return sonarUrl;
    }

    public void setSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GlobalConfigDataForSonarInstance that = (GlobalConfigDataForSonarInstance) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }
        if (pass != null ? !pass.equals(that.pass) : that.pass != null) {
            return false;
        }
        return sonarUrl != null ? sonarUrl.equals(that.sonarUrl) : that.sonarUrl == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (pass != null ? pass.hashCode() : 0);
        result = 31 * result + (sonarUrl != null ? sonarUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GlobalConfigDataForSonarInstance{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", pass='" + pass + '\'' +
                ", sonarUrl='" + sonarUrl + '\'' +
                '}';
    }
}
