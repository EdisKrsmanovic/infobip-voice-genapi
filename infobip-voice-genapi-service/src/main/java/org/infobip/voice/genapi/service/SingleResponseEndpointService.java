package org.infobip.voice.genapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;
import org.infobip.voice.genapi.provider.SingleResponseEndpointProvider;
import org.infobip.voice.genapi.model.SingleResponseEndpoint;
import org.infobip.voice.genapi.validator.SingleResponseEndpointValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintViolationException;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class SingleResponseEndpointService {

    private SingleResponseEndpointProvider singleResponseEndpointProvider;

    private SingleResponseEndpointValidator singleResponseEndpointValidator;

    public GenApiResponse<SingleResponseEndpoint> createSingleResponseEndpoint(SingleResponseEndpoint singleResponseEndpoint) {
        int statusCode = 200;
        String message = "OK";
        try {
            singleResponseEndpointValidator.checkIfValid(singleResponseEndpoint);
            singleResponseEndpointProvider.put(singleResponseEndpoint);
        } catch (DatabaseException exception) {
            log.warn(String.format("Failed to save Single Response Endpoint, message: %s", exception.getMessage()));
            statusCode = 503;
            message = exception.getMessage();
        } catch (ConstraintViolationException exception) {
            log.warn(String.format("Single Response Endpoint is not valid, message: %s", exception.getMessage()));
            statusCode = 400;
            message = exception.getMessage();
        }
        return generateGenApiResponse(statusCode, message, singleResponseEndpoint);
    }

    public GenApiResponse<SingleResponseEndpoint> getById(Integer singlResponseEndpointId) {
        try {
            SingleResponseEndpoint singleResponseEndpoint = singleResponseEndpointProvider.getById(singlResponseEndpointId);
            return generateGenApiResponse(200, "OK", singleResponseEndpoint);
        } catch (HttpEndpointNotFoundException e) {
            return generateGenApiResponse(404, "Not found", null);
        }
    }

    private GenApiResponse<SingleResponseEndpoint> generateGenApiResponse(Integer statusCode, String message, SingleResponseEndpoint singleResponseEndpoint) {
        return GenApiResponse.<SingleResponseEndpoint>builder()
                .statusCode(statusCode)
                .message(message)
                .entity(singleResponseEndpoint)
                .build();
    }
}
