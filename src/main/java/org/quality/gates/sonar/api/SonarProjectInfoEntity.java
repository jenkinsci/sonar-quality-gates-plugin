package org.quality.gates.sonar.api;

import java.util.Objects;

/**
 * @author arkanjoms
 * @since 1.0.1
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SonarProjectInfoEntity that = (SonarProjectInfoEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(key, that.key)
                && Objects.equals(qualifier, that.qualifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, qualifier);
    }

    @Override
    public String toString() {
        return "SonarProjectInfoEntity{" + "id='"
                + id + '\'' + ", key='"
                + key + '\'' + ", qualifier='"
                + qualifier + '\'' + '}';
    }
}
