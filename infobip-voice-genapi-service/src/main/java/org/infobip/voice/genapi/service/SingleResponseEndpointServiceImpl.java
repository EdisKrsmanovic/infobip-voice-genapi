package org.infobip.voice.genapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.spring.remoting.server.export.Export;
import org.infobip.voice.genapi.connector.model.EndpointResponse;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.connector.model.HttpHeader;
import org.infobip.voice.genapi.connector.service.SingleResponseEndpointService;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;
import org.infobip.voice.genapi.connector.model.SingleResponseEndpoint;
import org.infobip.voice.genapi.provider.SingleResponseEndpointProvider;
import org.infobip.voice.genapi.validator.EndpointValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;

@Slf4j
@Service
@Export(SingleResponseEndpointService.class)
@Validated
@AllArgsConstructor
public class SingleResponseEndpointServiceImpl implements EndpointService<SingleResponseEndpoint>, SingleResponseEndpointService {

    private SingleResponseEndpointProvider singleResponseEndpointProvider;

    private EndpointValidator endpointValidator;

    @Override
    public GenApiResponse getById(Integer singleResponseEndpointId) {
        try {
            log.info(String.format("Received request to get an endpoint by id %d", singleResponseEndpointId));
            SingleResponseEndpoint singleResponseEndpointById = singleResponseEndpointProvider.getById(singleResponseEndpointId);
            return generateGenApiResponse(200, "OK", singleResponseEndpointById);
        } catch (HttpEndpointNotFoundException e) {
            return generateGenApiResponse(404, "Not found", null);
        }
    }

    public GenApiResponse createEndpoint(SingleResponseEndpoint singleResponseEndpoint) {
        int statusCode = 200;
        String message = "OK";
        try {
            if (!endpointValidator.checkIfValid(singleResponseEndpoint)) {
                log.warn("Response Endpoint is not valid");
                statusCode = 400;
                message = "Response has invalid body";
            }
            else {
                singleResponseEndpointProvider.put(singleResponseEndpoint);
            }
        } catch (DatabaseException exception) {
            log.warn(String.format("Failed to save Single Response Endpoint, message: %s", exception.getMessage()));
            statusCode = 503;
            message = exception.getMessage();
        } catch (ConstraintViolationException exception) {
            log.warn(String.format("Some info is not correct, message: %s", exception.getMessage()));
            statusCode = 400;
            message = exception.getMessage();
        }
        log.info(String.format("Successfully created a single response endpoint with id %d", singleResponseEndpoint.getId()));
        return generateGenApiResponse(statusCode, message, singleResponseEndpoint);
    }

    public GenApiResponse updateEndpoint(SingleResponseEndpoint singleResponseEndpoint) {
        int statusCode = 200;
        String message = "OK";
        try {
            if(singleResponseEndpointProvider.getById(singleResponseEndpoint.getId()) == null) {
                log.warn("Failed to update non-existing Single Response Endpoint");
                statusCode = 404;
                message = "Cannot update non-existing single response endpoint";
            }
            else if (!endpointValidator.checkIfValid(singleResponseEndpoint)) {
                log.warn("Response Endpoint is not valid");
                statusCode = 400;
                message = "Response has invalid body";
            }
            else {
                singleResponseEndpointProvider.update(singleResponseEndpoint);
            }
        } catch (DatabaseException e) {
            log.warn(String.format("Failed to update Single Response Endpoint, message: %s", e.getMessage()));
            statusCode = 503;
            message = e.getMessage();
        } catch (HttpEndpointNotFoundException e) {
            log.warn(String.format("Failed to update non-existing Single Response Endpoint, message: %s", e.getMessage()));
            statusCode = 404;
            message = e.getMessage();
        }  catch (ConstraintViolationException exception) {
            log.warn(String.format("Some info is not correct, message: %s", exception.getMessage()));
            statusCode = 400;
            message = exception.getMessage();
        }
        return generateGenApiResponse(statusCode, message, singleResponseEndpoint);
    }

    private GenApiResponse<SingleResponseEndpoint> generateGenApiResponse(Integer statusCode, String message, SingleResponseEndpoint singleResponseEndpoint) {
        SingleResponseEndpoint singleResponseEndpointEntity = null;

        if(singleResponseEndpoint != null) {
            ArrayList<HttpHeader> httpHeaders = new ArrayList<>();
            singleResponseEndpoint.getHttpHeaders().forEach(e -> httpHeaders.add(new HttpHeader(e.getName(), e.getValue())));
            singleResponseEndpointEntity = new SingleResponseEndpoint(
                    singleResponseEndpoint.getId(),
                    singleResponseEndpoint.getHttpMethod(),
                    httpHeaders,
                    new EndpointResponse(singleResponseEndpoint.getResponse().getBody())
            );
        }

        return GenApiResponse.<SingleResponseEndpoint>builder()
                .statusCode(statusCode)
                .message(message)
                .entity(singleResponseEndpointEntity)
                .build();
    }
}
