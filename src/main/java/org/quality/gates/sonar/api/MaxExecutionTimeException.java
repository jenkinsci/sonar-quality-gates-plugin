package org.quality.gates.sonar.api;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
public class MaxExecutionTimeException extends RuntimeException {

    public MaxExecutionTimeException(String message) {
        super(message);
    }
}
