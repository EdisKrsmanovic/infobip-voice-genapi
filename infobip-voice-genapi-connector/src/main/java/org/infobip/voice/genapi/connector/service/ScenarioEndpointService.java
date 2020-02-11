package org.infobip.voice.genapi.connector.service;

import org.infobip.voice.genapi.connector.model.EndpointResponse;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.connector.model.ScenarioEndpoint;

public interface ScenarioEndpointService {
    /**
     * Creates scenario endpoint and stores it for further use
     * @param scenarioEndpoint object with headers and responses it should return in order you specify
     * @return GenApiResponse with status code, entity (scenarioEndpoint) and a message of success/failure
     */
    GenApiResponse<ScenarioEndpoint> createEndpoint(ScenarioEndpoint scenarioEndpoint);

    /**
     * Returns a scenarioEndpoint object that has same id given to the method
     * @param scenarioEndpointId that represents the id of a scenario endpoint that was returned when the scenario was created by createEndpoint
     * @return GenApiResponse with status code, requested entity (scenarioEndpoint) and a message of success/failure
     */
    GenApiResponse<ScenarioEndpoint> getById(Integer scenarioEndpointId);

    /**
     * Updates scenarioEndpoint with given values that has the same id as the scenarioEndpoint passed to the method
     * @param scenarioEndpoint that should be updated with its new values
     * @return GenApiResponse with status code, updated entity (scenarioEndpoint) and a message of success/failure
     */
    GenApiResponse<ScenarioEndpoint> updateEndpoint(ScenarioEndpoint scenarioEndpoint);

    /**
     * Creates a response for a scenario endpoint that has the same id given to the method.
     * Return order will be adjusted according to ordinal numbers of responses when creating/updating scenario endpoint
     * @param scenarioEndpointId Id of a scenario endpoint
     * @param endpointResponse endpointResponse that should scenario endpoint with given id return
     * @return GenApiResponse with status code, created entity(endpointResponse) and a message of success/failure
     */
    GenApiResponse<EndpointResponse> createScenarioEndpointResponse(Integer scenarioEndpointId, EndpointResponse endpointResponse);

    /**
     * Returns next response of a scenario endpoint with given id
     * @param scenarioEndpointId Id of a scenario endpoint
     * @return GenApiResponse with status code, requested entity(endpointResponse) and a message of success/failure
     */
    GenApiResponse<EndpointResponse> getNextResponse(Integer scenarioEndpointId);
}
