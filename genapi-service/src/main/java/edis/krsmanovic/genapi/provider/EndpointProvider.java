package edis.krsmanovic.genapi.provider;

import edis.krsmanovic.genapi.exception.DatabaseException;
import edis.krsmanovic.genapi.exception.HttpEndpointNotFoundException;

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
