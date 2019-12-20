package org.infobip.voice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.exception.DatabaseException;
import org.infobip.voice.model.HttpEndpoint;
import org.infobip.voice.provider.HttpEndpointProvider;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class HttpService {

    private HttpEndpointProvider httpEndpointProvider;

    public void createHttpEndpoint(@Valid HttpEndpoint httpEndpoint) {
        try {
            httpEndpointProvider.put(httpEndpoint);
        } catch (DatabaseException exception) {
            log.warn("Failed to save HttpEndpoint, message: " + exception.getMessage());
        }
    }
}
