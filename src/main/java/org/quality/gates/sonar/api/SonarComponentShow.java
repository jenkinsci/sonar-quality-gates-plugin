package org.quality.gates.sonar.api;

import java.util.List;

public class SonarComponentShow {

    private SonarProjectInfoEntity component;

    private List<SonarProjectInfoEntity> ancestors;

    public SonarProjectInfoEntity getComponent() {
        return component;
    }

    public void setComponent(SonarProjectInfoEntity component) {
        this.component = component;
    }

    public List<SonarProjectInfoEntity> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<SonarProjectInfoEntity> ancestors) {
        this.ancestors = ancestors;
    }
}
