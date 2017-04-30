package org.quality.gates.sonar.api;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
class UnsuportedVersionException extends RuntimeException {

    UnsuportedVersionException(String message) {
        super(message);
    }
}
