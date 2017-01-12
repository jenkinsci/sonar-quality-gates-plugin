package quality.gates.sonar.api;

/**
 * @author arkanjo.ms
 */
public class QualityGateTaskCE {

    private QualityGateTask[] queue;

    private QualityGateTask current;

    public QualityGateTask[] getQueue() {
        return queue;
    }

    public void setQueue(QualityGateTask[] queue) {
        this.queue = queue;
    }

    public QualityGateTask getCurrent() {
        return current;
    }

    public void setCurrent(QualityGateTask current) {
        this.current = current;
    }
}
