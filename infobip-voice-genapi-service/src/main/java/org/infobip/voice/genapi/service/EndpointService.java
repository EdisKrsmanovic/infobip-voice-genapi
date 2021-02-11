package org.infobip.voice.genapi.service;


import org.infobip.voice.genapi.model.GenApiResponse;

public interface EndpointService<T> {
    GenApiResponse<T> createEndpoint(T endpoint);
    GenApiResponse<T> getById(Integer endpointId);
    GenApiResponse<T> updateEndpoint(T endpoint);
}
