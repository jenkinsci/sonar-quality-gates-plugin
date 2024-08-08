package org.quality.gates.sonar.api;

import java.util.Objects;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
public class QualityGateTask {

    private String id;

    private String type;

    private String componentId;

    private String componenteName;

    private String componentQualifier;

    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponenteName() {
        return componenteName;
    }

    public void setComponenteName(String componenteName) {
        this.componenteName = componenteName;
    }

    public String getComponentQualifier() {
        return componentQualifier;
    }

    public void setComponentQualifier(String componentQualifier) {
        this.componentQualifier = componentQualifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityGateTask that = (QualityGateTask) o;
        return Objects.equals(id, that.id)
                && Objects.equals(type, that.type)
                && Objects.equals(componentId, that.componentId)
                && Objects.equals(componenteName, that.componenteName)
                && Objects.equals(componentQualifier, that.componentQualifier)
                && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, componentId, componenteName, componentQualifier, status);
    }

    @Override
    public String toString() {
        return "QualityGateTask{" + "id='"
                + id + '\'' + ", type='"
                + type + '\'' + ", componentId='"
                + componentId + '\'' + ", componenteName='"
                + componenteName + '\'' + ", componentQualifier='"
                + componentQualifier + '\'' + ", status='"
                + status + '\'' + '}';
    }
}
