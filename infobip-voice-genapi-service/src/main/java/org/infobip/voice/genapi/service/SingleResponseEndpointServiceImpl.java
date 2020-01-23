package org.infobip.voice.genapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infobip.spring.remoting.server.export.Export;
import org.infobip.voice.genapi.connector.model.GenApiResponse;
import org.infobip.voice.genapi.connector.model.HttpHeader;
import org.infobip.voice.genapi.connector.service.SingleResponseEndpointService;
import org.infobip.voice.genapi.exception.DatabaseException;
import org.infobip.voice.genapi.exception.HttpEndpointNotFoundException;
import org.infobip.voice.genapi.model.EndpointResponse;
import org.infobip.voice.genapi.provider.SingleResponseEndpointProvider;
import org.infobip.voice.genapi.model.SingleResponseEndpoint;
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
public class SingleResponseEndpointServiceImpl implements EndpointService<org.infobip.voice.genapi.connector.model.SingleResponseEndpoint>, SingleResponseEndpointService {

    private SingleResponseEndpointProvider singleResponseEndpointProvider;

    private EndpointValidator endpointValidator;


    @Override
    public GenApiResponse createEndpoint(org.infobip.voice.genapi.connector.model.SingleResponseEndpoint singleResponseEndpoint) {
        log.info("Received request to create an endpoint");
        SingleResponseEndpoint convertedSingleResponseEndpoint = convertEndpoint(singleResponseEndpoint);
        return createEndpoint(convertedSingleResponseEndpoint);
    }

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

    @Override
    public GenApiResponse updateEndpoint(org.infobip.voice.genapi.connector.model.SingleResponseEndpoint singleResponseEndpoint) {
        log.info(String.format("Received request to update an endpoint by id %d", singleResponseEndpoint.getId()));
        SingleResponseEndpoint convertedSingleResponseEndpoint = convertEndpoint(singleResponseEndpoint);
        return updateEndpoint(convertedSingleResponseEndpoint);
    }

    public GenApiResponse createEndpoint(SingleResponseEndpoint singleResponseEndpoint) {
        int statusCode = 200;
        String message = "OK";
        try {
            if (!endpointValidator.checkIfValid(singleResponseEndpoint)) {
                throw new ConstraintViolationException("Response has invalid body.", null);
            }
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
        log.info(String.format("Successfully created a single response endpoint with id %d", singleResponseEndpoint.getId()));
        return generateGenApiResponse(statusCode, message, singleResponseEndpoint);
    }

    private GenApiResponse updateEndpoint(SingleResponseEndpoint singleResponseEndpoint) {
        int statusCode = 200;
        String message = "OK";
        try {
            singleResponseEndpointProvider.update(singleResponseEndpoint);
        } catch (DatabaseException e) {
            log.warn(String.format("Failed to update Single Response Endpoint, message: %s", e.getMessage()));
            statusCode = 503;
            message = e.getMessage();
        }
        return generateGenApiResponse(statusCode, message, singleResponseEndpoint);
    }

    private SingleResponseEndpoint convertEndpoint(org.infobip.voice.genapi.connector.model.SingleResponseEndpoint singleResponseEndpoint) {
        try {
            SingleResponseEndpoint singleResponseEndpoint1 = new SingleResponseEndpoint(
                    singleResponseEndpoint.getId(),
                    singleResponseEndpoint.getHttpMethod(),
                    new ArrayList<HttpHeader>(),
                    new EndpointResponse(singleResponseEndpoint.getResponse().getBody())
            );
            singleResponseEndpoint.getHttpHeaders().forEach(e -> singleResponseEndpoint1.getHttpHeaders().add(new HttpHeader(e.getName(), e.getValue())));
            return singleResponseEndpoint1;
        } catch (Throwable ex) {
            log.error(String.format("Something went wrong, error: %s", ex.getMessage()));
            ex.printStackTrace();
        }
        return null;
    }


    private GenApiResponse<org.infobip.voice.genapi.connector.model.SingleResponseEndpoint> generateGenApiResponse(Integer statusCode, String message, SingleResponseEndpoint singleResponseEndpoint) {
        org.infobip.voice.genapi.connector.model.SingleResponseEndpoint singleResponseEndpointEntity = null;

        if(singleResponseEndpoint != null) {
            ArrayList<org.infobip.voice.genapi.connector.model.HttpHeader> httpHeaders = new ArrayList<>();
            singleResponseEndpoint.getHttpHeaders().forEach(e -> httpHeaders.add(new org.infobip.voice.genapi.connector.model.HttpHeader(e.getName(), e.getValue())));
            singleResponseEndpointEntity = new org.infobip.voice.genapi.connector.model.SingleResponseEndpoint(
                    singleResponseEndpoint.getId(),
                    singleResponseEndpoint.getHttpMethod(),
                    httpHeaders,
                    new org.infobip.voice.genapi.connector.model.EndpointResponse(singleResponseEndpoint.getResponse().getBody())
            );
        }


        return GenApiResponse.<org.infobip.voice.genapi.connector.model.SingleResponseEndpoint>builder()
                .statusCode(statusCode)
                .message(message)
                .entity(singleResponseEndpointEntity)
                .build();
    }
}
