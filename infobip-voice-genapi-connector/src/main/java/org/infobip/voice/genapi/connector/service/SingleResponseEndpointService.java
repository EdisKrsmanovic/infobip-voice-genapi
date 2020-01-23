package org.infobip.voice.genapi.connector.service;

import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.connector.model.SingleResponseEndpoint;

public interface SingleResponseEndpointService {
    GenApiResponse<SingleResponseEndpoint> createEndpoint(SingleResponseEndpoint singleResponseEndpoint);

    GenApiResponse<SingleResponseEndpoint> getById(Integer singleResponseEndpointId);

    GenApiResponse<SingleResponseEndpoint> updateEndpoint(SingleResponseEndpoint singleResponseEndpoint);
}
