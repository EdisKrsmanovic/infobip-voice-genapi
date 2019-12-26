package org.infobip.voice.genapi.connector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;

@AllArgsConstructor
@Builder
public final class GenApiResponse<T> implements Serializable {

    private final Integer statusCode;
    private final T httpEndpoint;
    private final String message;

    public Integer getStatusCode() {
        return statusCode;
    }

    public T getHttpEndpoint() {
        return httpEndpoint;
    }

    public String getMessage() {
        return message;
    }
}
