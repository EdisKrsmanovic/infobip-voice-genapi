package org.infobip.voice.genapi.connector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;

@AllArgsConstructor
@Builder
public final class GenApiResponse<T> implements Serializable {

    private final Integer statusCode;
    private final T entity;
    private final String message;

    public Integer getStatusCode() {
        return statusCode;
    }

    public T getEntity() {
        return entity;
    }

    public String getMessage() {
        return message;
    }
}
