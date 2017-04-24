package org.quality.gates.jenkins.plugin;

public class QGException extends RuntimeException {

    public QGException() {
        //default
    }

    public QGException(String message) {
        super(message);
    }

    public QGException(String message, Throwable cause) {
        super(message, cause);
    }

    public QGException(Throwable cause) {
        super(cause);
    }
}
