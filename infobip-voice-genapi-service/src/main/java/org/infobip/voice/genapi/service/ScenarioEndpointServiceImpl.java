package org.infobip.voice.genapi.service;

import com.hazelcast.core.IMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.spring.remoting.server.export.Export;
import org.infobip.validation.api.ValidatedRmiService;
import org.infobip.voice.genapi.connector.model.EndpointResponse;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.connector.model.ScenarioEndpoint;
import org.infobip.voice.genapi.connector.service.ScenarioEndpointService;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;
import org.infobip.voice.genapi.provider.ScenarioEndpointProvider;
import org.infobip.voice.genapi.validator.EndpointValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@Validated
@AllArgsConstructor
@Export(ScenarioEndpointService.class)
@ValidatedRmiService(ScenarioEndpointService.class)
public class ScenarioEndpointServiceImpl implements EndpointService<ScenarioEndpoint>, ScenarioEndpointService {

    private ScenarioEndpointProvider scenarioEndpointProvider;

    private EndpointValidator endpointValidator;

    private IMap<Integer, Integer> nextResponseMap;

    public GenApiResponse<ScenarioEndpoint> createEndpoint(ScenarioEndpoint scenarioEndpoint) {
        int statusCode = 200;
        String message = "OK";
        try {
            if (!endpointValidator.checkIfValidScenario(scenarioEndpoint)) {
                log.warn("Received a scenario endpoint that has invalid response body");
                statusCode = 400;
                message = "One of the given responses body is invalid";
            } else {
                List<EndpointResponse> endpointResponses = scenarioEndpoint.getEndpointResponses();
                if (endpointResponses.stream().anyMatch(e -> e.getOrdinalNumber() == null)) {
                    endpointResponses.forEach(e -> e.setOrdinalNumber(endpointResponses.indexOf(e)));
                    message = "OK, all ordinal numbers set to their order in given array";
                }
                scenarioEndpointProvider.put(scenarioEndpoint);
            }
        } catch (DatabaseException exception) {
            log.warn("Failed to save Scenario Endpoint, message: {}", exception.getMessage());
            statusCode = 503;
            message = exception.getMessage();
        } catch (ConstraintViolationException exception) {
            log.warn("Some info is not correct, message: {}", exception.getMessage());
            statusCode = 400;
            message = exception.getMessage();
            scenarioEndpoint = null;
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
            if (scenarioEndpoint.getId() == null || scenarioEndpointProvider.getById(scenarioEndpoint.getId()) == null) {
                log.warn("Endpoint with id of a given scenario endpoint does not exist.");
                statusCode = 404;
                message = "Cannot update a scenario endpoint that does not exist with a given id";
            } else if (!endpointValidator.checkIfValidScenario(scenarioEndpoint)) {
                log.warn("Received endpoint whose one of the responses has invalid body");
                statusCode = 400;
                message = "One of the responses of given scenario endpoint has invalid body";
            } else {
                List<EndpointResponse> endpointResponses = scenarioEndpoint.getEndpointResponses();
                if (endpointResponses.stream().anyMatch(e -> e.getOrdinalNumber() == null)) {
                    endpointResponses.forEach(e -> e.setOrdinalNumber(endpointResponses.indexOf(e)));
                    message = "OK, all ordinal numbers set to their order in given array";
                }
                scenarioEndpointProvider.update(scenarioEndpoint);
                nextResponseMap.set(scenarioEndpoint.getId(), scenarioEndpoint.getNextResponseNo().intValue());
            }
        } catch (DatabaseException e) {
            log.warn("Failed to update Scenario Endpoint, message: {}", e.getMessage());
            statusCode = 503;
            message = e.getMessage();
        } catch (HttpEndpointNotFoundException e) {
            log.warn("Failed to update non-existing Scenario Endpoint, message: {}", e.getMessage());
            statusCode = 404;
            message = e.getMessage();
        } catch (ConstraintViolationException exception) {
            log.warn("Some info is not correct, message: {}", exception.getMessage());
            statusCode = 400;
            message = exception.getMessage();
        }
        return generateGenApiResponse(statusCode, message, scenarioEndpoint);
    }

    public GenApiResponse<EndpointResponse> createScenarioEndpointResponse(Integer scenarioEndpointId, EndpointResponse endpointResponse) {
        int statusCode = 200;
        String message = "OK";
        try {
            if (endpointResponse.getBody() == null || !endpointValidator.isJSONValid(endpointResponse.getBody())) {
                log.warn("Received response that has invalid body");
                statusCode = 400;
                message = "Response has invalid body";
            } else {
                scenarioEndpointProvider.put(scenarioEndpointId, endpointResponse);
            }
        } catch (HttpEndpointNotFoundException e) {
            log.warn("Scenario Endpoint not found, message: {}", e.getMessage());
            statusCode = 404;
            message = "Not found";
        } catch (ConstraintViolationException exception) {
            log.warn("Some info is not correct, message: {}", exception.getMessage());
            statusCode = 400;
            message = exception.getMessage();
        }
        return generateGenApiResponse(statusCode, message, endpointResponse);
    }

    public GenApiResponse<EndpointResponse> getNextResponse(Integer scenarioEndpointId) {
        GenApiResponse<ScenarioEndpoint> genApiResponse = getById(scenarioEndpointId);
        ScenarioEndpoint scenarioEndpoint = genApiResponse.getEntity();
        if (scenarioEndpoint != null) {
            nextResponseMap.set(scenarioEndpointId, scenarioEndpoint.getNextResponseNo().intValue());

            if (scenarioEndpoint.getEndpointResponses().isEmpty()) {
                return generateGenApiResponse(404, String.format("No responses found for Scenario Endpoint with id %s", scenarioEndpointId), null);
            }

            Integer nextResponseIndex = nextResponseMap.get(scenarioEndpointId);
            if (nextResponseIndex == null) {
                nextResponseMap.put(scenarioEndpointId, 0);
                scenarioEndpoint.setNextResponseNo(new AtomicInteger(0));
                nextResponseIndex = 0;
            } else if (nextResponseIndex >= scenarioEndpoint.getEndpointResponses().size()) {
                return generateGenApiResponse(204, "No more responses", null);
            }
            EndpointResponse endpointResponse = scenarioEndpoint.getEndpointResponses().get(nextResponseIndex);

            nextResponseMap.put(scenarioEndpointId, nextResponseIndex + 1);
            scenarioEndpoint.setNextResponseNo(new AtomicInteger(scenarioEndpoint.getNextResponseNo().intValue()+1));
            return generateGenApiResponse(200, "OK", endpointResponse);
        } else {
            return generateGenApiResponse(404, String.format("Scenario endpoint with id %d not found", scenarioEndpointId), null);
        }
    }

    private <T> GenApiResponse<T> generateGenApiResponse(Integer statusCode, String message, T entity) {
        return GenApiResponse.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .entity(entity)
                .build();
    }
}