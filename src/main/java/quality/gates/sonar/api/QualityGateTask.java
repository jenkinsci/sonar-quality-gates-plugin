package quality.gates.sonar.api;

/**
 * "id": "AVmLTzD-30JeYeWRwb-W",
 * "type": "REPORT",
 * "componentId": "AVQ0HTEMy6i7tgjiPW9U",
 * "componentKey": "br.com.azi:efcaz-api",
 * "componentName": "efcaz-api",
 * "componentQualifier": "TRK",
 * "status": "IN_PROGRESS",
 *
 * @author arkanjo.ms
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
}
