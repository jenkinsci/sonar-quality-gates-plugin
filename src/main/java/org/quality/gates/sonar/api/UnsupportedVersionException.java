package org.quality.gates.sonar.api;

/**
 * @author arkanjoms
 * @since 1.0.1
 */
class UnsupportedVersionException extends RuntimeException {

    UnsupportedVersionException(String message) {
        super(message);
    }
}
