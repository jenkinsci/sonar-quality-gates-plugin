package org.quality.gates.sonar.api;

import java.util.Objects;

public class QualityGatesStatus {

    public static final String BUILDS = "OK";

    private final String status;

    public QualityGatesStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public boolean hasStatusGreen() {
        return BUILDS.equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualityGatesStatus that = (QualityGatesStatus) o;
        return Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(status);
    }

    @Override
    public String toString() {
        return "QualityGatesStatus{" + "status='" + status + '\'' + '}';
    }
}
