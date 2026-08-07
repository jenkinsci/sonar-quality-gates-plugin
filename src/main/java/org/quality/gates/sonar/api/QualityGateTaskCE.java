package org.quality.gates.sonar.api;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
public class QualityGateTaskCE {

    private QualityGateTask[] queue;

    private QualityGateTask current;

    public QualityGateTask[] getQueue() {
        return queue == null ? null : queue.clone();
    }

    public void setQueue(QualityGateTask[] queue) {
        this.queue = queue == null ? null : queue.clone();
    }

    public QualityGateTask getCurrent() {
        return current;
    }

    public void setCurrent(QualityGateTask current) {
        this.current = current;
    }
}
