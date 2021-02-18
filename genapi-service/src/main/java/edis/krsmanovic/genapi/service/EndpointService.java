package edis.krsmanovic.genapi.service;


import edis.krsmanovic.genapi.model.GenApiResponse;

public interface EndpointService<T> {
    GenApiResponse<T> createEndpoint(T endpoint);
    GenApiResponse<T> getById(Integer endpointId);
    GenApiResponse<T> updateEndpoint(T endpoint);
}
