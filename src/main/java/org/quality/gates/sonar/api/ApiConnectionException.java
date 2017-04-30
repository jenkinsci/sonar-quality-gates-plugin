package org.quality.gates.sonar.api;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
public class ApiConnectionException extends RuntimeException {

    public ApiConnectionException(String message) {
        super(message);
    }
}
