package org.infobip.voice.exception;

public class HttpEndpointNotFoundException extends Exception {
    public HttpEndpointNotFoundException(Integer httpEndpointId, Exception e) {
        super(String.format("Could not find Http Endpoint with id %s, message: %s", httpEndpointId, e.getMessage()));
    }
}
