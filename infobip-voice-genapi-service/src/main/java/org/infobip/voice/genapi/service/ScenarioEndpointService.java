package org.infobip.voice.genapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;
import org.infobip.voice.genapi.model.EndpointResponse;
import org.infobip.voice.genapi.model.ScenarioEndpoint;
import org.infobip.voice.genapi.provider.ScenarioEndpointProvider;
import org.infobip.voice.genapi.validator.EndpointValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintViolationException;
import java.util.List;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class ScenarioEndpointService implements EndpointService<ScenarioEndpoint> {

    private ScenarioEndpointProvider scenarioEndpointProvider;

    private EndpointValidator endpointValidator;

    public GenApiResponse<ScenarioEndpoint> createEndpoint(ScenarioEndpoint scenarioEndpoint) {
        int statusCode = 200;
        String message = "OK";
        try {
            if (!endpointValidator.checkIfValidScenario(scenarioEndpoint)) {
                throw new ConstraintViolationException("One of the responses has invalid body.", null);
            }
            List<EndpointResponse> endpointResponses = scenarioEndpoint.getEndpointResponses();
            endpointResponses.forEach(e -> e.setOrdinalNumber(endpointResponses.indexOf(e)));
            scenarioEndpointProvider.put(scenarioEndpoint);
        } catch (DatabaseException exception) {
            log.warn(String.format("Failed to save Scenario Endpoint, message: %s", exception.getMessage()));
            statusCode = 503;
            message = exception.getMessage();
        } catch (ConstraintViolationException exception) {
            log.warn(String.format("Scenario Endpoint is not valid, message: %s", exception.getMessage()));
            statusCode = 400;
            message = exception.getMessage();
        }
        return generateGenApiResponse(statusCode, message, scenarioEndpoint);
    }

    public GenApiResponse<ScenarioEndpoint> getById(Integer scenarioEndpointId) {
        try {
            ScenarioEndpoint scenarioEndpoint = scenarioEndpointProvider.getById(scenarioEndpointId);
            return generateGenApiResponse(200, "OK", scenarioEndpoint);
        } catch (HttpEndpointNotFoundException e) {
            return generateGenApiResponse(404, "Not found", null);
        }
    }

    public GenApiResponse<ScenarioEndpoint> updateEndpoint(ScenarioEndpoint scenarioEndpoint) {
        int statusCode = 200;
        String message = "OK";
        try {
            scenarioEndpointProvider.update(scenarioEndpoint);
        } catch (DatabaseException e) {
            log.warn(String.format("Failed to update Scenario Endpoint, message: %s", e.getMessage()));
            statusCode = 503;
            message = e.getMessage();
        }
        return generateGenApiResponse(statusCode, message, scenarioEndpoint);
    }

    public GenApiResponse<EndpointResponse> createScenarioEndpointResponse(Integer scenarioEndpointId, EndpointResponse endpointResponse) {
        int statusCode = 200;
        String message = "OK";
        try {
            scenarioEndpointProvider.put(scenarioEndpointId, endpointResponse);
        } catch (HttpEndpointNotFoundException e) {
            log.warn(String.format("Scenario Endpoint not found, message: %s", e.getMessage()));
            statusCode = 404;
            message = "Not found";
        } catch (Exception e) {
            log.warn(String.format("Failed to save response for Scenario Endpoint, message: %s", e.getMessage()));
            statusCode = 503;
            message = e.getMessage();
        }
        return generateGenApiResponse(statusCode, message, endpointResponse);
    }

    public GenApiResponse<EndpointResponse> getNextResponse(Integer scenarioEndpointId) {
        GenApiResponse<ScenarioEndpoint> genApiResponse = getById(scenarioEndpointId);
        ScenarioEndpoint scenarioEndpoint = genApiResponse.getEntity();

        if (scenarioEndpoint.getEndpointResponses().isEmpty()) {
            return generateGenApiResponse(404, String.format("No responses found for Scenario Endpoint with id %s", scenarioEndpointId), null);
        }

        EndpointResponse endpointResponse = scenarioEndpoint.getResponse();
        return generateGenApiResponse(200, "OK", endpointResponse);
    }

    private <T> GenApiResponse<T> generateGenApiResponse(Integer statusCode, String message, T entity) {
        return GenApiResponse.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .entity(entity)
                .build();
    }
}