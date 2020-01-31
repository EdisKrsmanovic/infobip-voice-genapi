package org.infobip.voice.genapi.exception;

public class HttpEndpointNotFoundException extends Exception {
    public HttpEndpointNotFoundException(Integer httpEndpointId, Exception e) {
        super(String.format("Could not find Http Endpoint with id {}, message: {}", httpEndpointId, e.getMessage()));
    }
}
