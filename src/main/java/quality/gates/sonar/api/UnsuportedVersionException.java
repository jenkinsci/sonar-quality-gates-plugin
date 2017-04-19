package quality.gates.sonar.api;

/**
 * @author arkanjoms
 * @since 1.0
 */
class UnsuportedVersionException extends RuntimeException {

    UnsuportedVersionException(String message) {
        super(message);
    }
}
