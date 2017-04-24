package org.quality.gates.sonar.api;

/**
 * Created by arkanjo on 22/04/17.
 */
public class MaxExecutionTimeException extends RuntimeException {

    public MaxExecutionTimeException(String message) {
        super(message);
    }
}
