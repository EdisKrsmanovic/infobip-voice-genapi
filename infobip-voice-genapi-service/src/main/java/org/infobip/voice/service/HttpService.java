package org.infobip.voice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.exception.DatabaseException;
import org.infobip.voice.model.HttpEndpoint;
import org.infobip.voice.repository.HttpEndpointRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Slf4j
@Service
@AllArgsConstructor
@Validated
public class HttpService {

    private HttpEndpointRepository httpEndpointRepository;

    public void createHttpEndpoint(@Valid HttpEndpoint httpEndpoint) {
        try {
            httpEndpointRepository.save(httpEndpoint);
        } catch (DatabaseException exception) {
            log.warn("Failed to save HttpEndpoint, message: " + exception.getMessage());
        }
    }
}
