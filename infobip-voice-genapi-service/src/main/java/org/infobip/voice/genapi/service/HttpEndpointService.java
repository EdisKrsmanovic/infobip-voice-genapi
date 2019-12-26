package org.infobip.voice.genapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;
import org.infobip.voice.genapi.provider.HttpEndpointProvider;
import org.infobip.voice.genapi.model.HttpEndpoint;
import org.infobip.voice.genapi.validator.HttpEndpointValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintViolationException;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class HttpEndpointService {

    private HttpEndpointProvider httpEndpointProvider;

    private HttpEndpointValidator httpEndpointValidator;

    public GenApiResponse<HttpEndpoint> createHttpEndpoint(HttpEndpoint httpEndpoint) {
        int statusCode = 200;
        String message = "OK";
        try {
            httpEndpointValidator.checkIfValid(httpEndpoint);
            httpEndpointProvider.put(httpEndpoint);
        } catch (DatabaseException exception) {
            log.warn(String.format("Failed to save HttpEndpoint, message: %s", exception.getMessage()));
            statusCode = 503;
            message = exception.getMessage();
        } catch (ConstraintViolationException exception) {
            log.warn(String.format("HttpEndpoint is not valid, message: %s", exception.getMessage()));
            statusCode = 400;
            message = exception.getMessage();
        }
        return generateGenApiResponse(statusCode, message, httpEndpoint);
    }

    public GenApiResponse<HttpEndpoint> getById(Integer httpEndpointId) {
        try {
            HttpEndpoint httpEndpoint = httpEndpointProvider.getById(httpEndpointId);
            return generateGenApiResponse(200, "OK", httpEndpoint);
        } catch (HttpEndpointNotFoundException e) {
            return generateGenApiResponse(404, "Not found", null);
        }
    }

    private GenApiResponse<HttpEndpoint> generateGenApiResponse(Integer statusCode, String message, HttpEndpoint httpEndpoint) {
        return GenApiResponse.<HttpEndpoint>builder()
                .statusCode(statusCode)
                .message(message)
                .httpEndpoint(httpEndpoint)
                .build();
    }
}
