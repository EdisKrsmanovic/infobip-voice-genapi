package edis.krsmanovic.genapi.service;


import edis.krsmanovic.genapi.model.GenApiResponse;
import edis.krsmanovic.genapi.model.SingleResponseEndpoint;

public interface SingleResponseEndpointService {
    /**
     * Creates single response endpoint and stores it for further use
     * @param singleResponseEndpoint object with headers and response body it should return
     * @return GenApiResponse with status code, entity (singleResponseEndpoint) and a message of success/failure
     */
    GenApiResponse<SingleResponseEndpoint> createEndpoint(SingleResponseEndpoint singleResponseEndpoint);

    /**
     * Returns a scenarioEndpoint object that has same id given to the method
     * @param singleResponseEndpointId that represents the id of a single response endpoint that was returned when the endpoint was created by createEndpoint
     * @return GenApiResponse with status code, requested entity (singleResponseEndpoint) and a message of success/failure
     */
    GenApiResponse<SingleResponseEndpoint> getById(Integer singleResponseEndpointId);

    /**
     * Updates singleResponseENdpoint with given values that has the same id as the singleResponseEndpoint passed to the method
     * @param singleResponseEndpoint that should be updated with its new values
     * @return GenApiResponse with status code, updated entity (singleResponseEndpoint) and a message of success/failure
     */
    GenApiResponse<SingleResponseEndpoint> updateEndpoint(SingleResponseEndpoint singleResponseEndpoint);
}
