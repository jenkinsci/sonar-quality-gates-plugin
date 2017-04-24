package org.quality.gates.sonar.api;

/**
 * Created by arkanjo on 22/04/17.
 */
public class SonarProjectInfoEntity {

    private String id;

    private String key;

    private String qualifier;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }
}
