package edis.krsmanovic.genapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import edis.krsmanovic.genapi.exception.DatabaseException;
import edis.krsmanovic.genapi.exception.HttpEndpointNotFoundException;
import edis.krsmanovic.genapi.model.EndpointResponse;
import edis.krsmanovic.genapi.model.GenApiResponse;
import edis.krsmanovic.genapi.model.HttpHeader;
import edis.krsmanovic.genapi.model.SingleResponseEndpoint;
import edis.krsmanovic.genapi.provider.SingleResponseEndpointProvider;
import edis.krsmanovic.genapi.validator.EndpointValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class SingleResponseEndpointServiceImpl implements EndpointService<SingleResponseEndpoint>, SingleResponseEndpointService {

    private final SingleResponseEndpointProvider singleResponseEndpointProvider;

    private final EndpointValidator endpointValidator;

    @Override
    public GenApiResponse<SingleResponseEndpoint> getById(Integer singleResponseEndpointId) {
        try {
            log.info("Received request to get an endpoint by id {}", singleResponseEndpointId);
            SingleResponseEndpoint singleResponseEndpointById = singleResponseEndpointProvider.getById(singleResponseEndpointId);
            log.info("Successfully returned a single response endpoint with id {}", singleResponseEndpointId);
            return generateGenApiResponse(200, "OK", singleResponseEndpointById);
        } catch (HttpEndpointNotFoundException e) {
            return generateGenApiResponse(404, "Not found", null);
        }
    }

    public GenApiResponse<SingleResponseEndpoint> createEndpoint(SingleResponseEndpoint singleResponseEndpoint) {
        log.info("Received a request to create a single response endpoint");
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
                log.info("Successfully created a single response endpoint with id {}", singleResponseEndpoint.getId());
            }
        } catch (DatabaseException exception) {
            log.warn("Failed to save Single Response Endpoint, message: {}", exception.getMessage());
            statusCode = 503;
            message = exception.getMessage();
        } catch (ConstraintViolationException exception) {
            log.warn("Some info is not correct, message: {}", exception.getMessage());
            statusCode = 400;
            message = exception.getMessage();
        }
        return generateGenApiResponse(statusCode, message, singleResponseEndpoint);
    }

    public GenApiResponse<SingleResponseEndpoint> updateEndpoint(SingleResponseEndpoint singleResponseEndpoint) {
        log.info("Received a request to update a single response endpoint with id {}", singleResponseEndpoint.getId());
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
                log.info("Successfully updated");
            }
        } catch (DatabaseException e) {
            log.warn("Failed to update Single Response Endpoint, message: {}", e.getMessage());
            statusCode = 503;
            message = e.getMessage();
        } catch (HttpEndpointNotFoundException e) {
            log.warn("Failed to update non-existing Single Response Endpoint, message: {}", e.getMessage());
            statusCode = 404;
            message = e.getMessage();
        }  catch (ConstraintViolationException exception) {
            log.warn("Some info is not correct, message: {}", exception.getMessage());
            statusCode = 400;
            message = exception.getMessage();
        }
        return generateGenApiResponse(statusCode, message, singleResponseEndpoint);
    }

    private GenApiResponse<SingleResponseEndpoint> generateGenApiResponse(Integer statusCode, String message, SingleResponseEndpoint singleResponseEndpoint) {
        return GenApiResponse.<SingleResponseEndpoint>builder()
                .statusCode(statusCode)
                .message(message)
                .entity(singleResponseEndpoint)
                .build();
    }
}
