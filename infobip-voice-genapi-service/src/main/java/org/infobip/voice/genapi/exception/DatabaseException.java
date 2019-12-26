package org.infobip.voice.genapi.exception;

public class DatabaseException extends Exception {

    public DatabaseException(String message) {
        super(String.format("Could not save HttpEndpoint to database, message: %s", message));
    }
}
