package org.infobip.voice.genapi.provider;

import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;

public interface EndpointProvider<T> {
    long reloadAll();
    T reloadId(Integer endpointId);
    T getById(Integer endpointId) throws HttpEndpointNotFoundException;
    int put(T endpoint) throws DatabaseException;
    void remove(Integer endpointId) throws DatabaseException;
    void clear();
    void update(T endpoint) throws DatabaseException;
    long size();
}
