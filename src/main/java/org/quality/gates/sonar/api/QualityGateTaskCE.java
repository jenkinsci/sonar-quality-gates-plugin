package org.quality.gates.sonar.api;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
public class QualityGateTaskCE {

    private QualityGateTask[] queue;

    private QualityGateTask current;

    public QualityGateTask[] getQueue() {
        return (QualityGateTask[]) ArrayUtils.clone(queue);
    }

    public void setQueue(QualityGateTask[] queue) {
        this.queue = (QualityGateTask[]) ArrayUtils.clone(queue);
    }

    public QualityGateTask getCurrent() {
        return current;
    }

    public void setCurrent(QualityGateTask current) {
        this.current = current;
    }
}
