package org.infobip.voice.genapi.connector.service;

import org.infobip.voice.genapi.connector.model.EndpointResponse;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.connector.model.ScenarioEndpoint;

public interface ScenarioEndpointService {
    GenApiResponse<ScenarioEndpoint> createEndpoint(ScenarioEndpoint scenarioEndpoint);

    GenApiResponse<ScenarioEndpoint> getById(Integer singleResponseEndpointId);

    GenApiResponse<ScenarioEndpoint> updateEndpoint(ScenarioEndpoint scenarioEndpoint);

    GenApiResponse<EndpointResponse> createScenarioEndpointResponse(Integer scenarioEndpointId, EndpointResponse endpointResponse);

    GenApiResponse<EndpointResponse> getNextResponse(Integer scenarioEndpointId);
}
