package org.quality.gates.sonar.api;

public class CredentialsNotConfiguredException extends RuntimeException {
    public CredentialsNotConfiguredException(String message) {
        super(message);
    }
}
