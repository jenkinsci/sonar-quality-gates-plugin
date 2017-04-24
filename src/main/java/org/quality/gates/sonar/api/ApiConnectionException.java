package org.quality.gates.sonar.api;

/**
 * Created by arkanjo on 21/04/17.
 */
public class ApiConnectionException extends RuntimeException {

    public ApiConnectionException(String message) {
        super(message);
    }
}
