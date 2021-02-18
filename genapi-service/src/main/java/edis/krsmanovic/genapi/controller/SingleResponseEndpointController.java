package edis.krsmanovic.genapi.controller;

import lombok.AllArgsConstructor;
import edis.krsmanovic.genapi.encryptor.JasyptIdEncryptor;
import edis.krsmanovic.genapi.exception.InvalidIdException;
import edis.krsmanovic.genapi.model.GenApiResponse;
import edis.krsmanovic.genapi.model.SingleResponseEndpoint;
import edis.krsmanovic.genapi.model.SingleResponseEndpointWrapper;
import edis.krsmanovic.genapi.service.SingleResponseEndpointService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("endpoint")
@AllArgsConstructor
public class SingleResponseEndpointController {

    private final SingleResponseEndpointService singleResponseEndpointService;
    private final JasyptIdEncryptor jasyptIdEncryptor;

    @PostMapping(path = "create")
    public GenApiResponse<SingleResponseEndpointWrapper> createEndpoint(@RequestBody SingleResponseEndpoint singleResponseEndpoint) {
        GenApiResponse<SingleResponseEndpoint> serviceResponse = singleResponseEndpointService.createEndpoint(singleResponseEndpoint);

        return generateGenApiResponse(serviceResponse);
    }

    @GetMapping(path = "get/{id}")
    public GenApiResponse<SingleResponseEndpointWrapper> getEndpointById(@PathVariable("id") String encryptedId) {
        try {
            Integer decryptedId = jasyptIdEncryptor.decryptId(encryptedId);
            GenApiResponse<SingleResponseEndpoint> serviceResponse = singleResponseEndpointService.getById(decryptedId);

            return generateGenApiResponse(serviceResponse);
        } catch (InvalidIdException e) {
            return GenApiResponse.<SingleResponseEndpointWrapper>builder()
                    .statusCode(400)
                    .entity(null)
                    .message(e.getMessage())
                    .build();
        }
    }

    @RequestMapping(path = "getResponse/{id}", produces = "application/json")
    public ResponseEntity<String> getEndpointResponseById(@PathVariable("id") String encryptedId, final HttpServletRequest request) {
        try {
            Integer decryptedId = jasyptIdEncryptor.decryptId(encryptedId);
            GenApiResponse<SingleResponseEndpoint> serviceResponse = singleResponseEndpointService.getById(decryptedId);

            if(!serviceResponse.getEntity().getHttpMethod().toString().equalsIgnoreCase(request.getMethod())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endpoint not found (check your http method)");
            }

            HttpHeaders headers = new HttpHeaders();
            serviceResponse
                    .getEntity()
                    .getHttpHeaders()
                    .forEach(header -> headers.add(header.getName(), header.getValue()));

            return new ResponseEntity<>(serviceResponse.getEntity().getResponse().getBody(), headers, HttpStatus.OK);
        } catch (InvalidIdException e) {
            return new ResponseEntity<>("{\"message\": \"" + e.getMessage() + "\"}", null, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(path = "update")
    public GenApiResponse<SingleResponseEndpointWrapper> updateEndpoint(@RequestBody SingleResponseEndpointWrapper singleResponseEndpoint) {
        try {
            GenApiResponse<SingleResponseEndpoint> serviceResponse = singleResponseEndpointService.updateEndpoint(
                    new SingleResponseEndpoint(
                            jasyptIdEncryptor.decryptId(singleResponseEndpoint.getId()),
                            singleResponseEndpoint.getHttpMethod(),
                            singleResponseEndpoint.getHttpHeaders(),
                            singleResponseEndpoint.getResponse()
                    )
            );

            return generateGenApiResponse(serviceResponse);
        } catch (InvalidIdException e) {
            return GenApiResponse.<SingleResponseEndpointWrapper>builder()
                    .statusCode(400)
                    .entity(null)
                    .message(e.getMessage())
                    .build();
        }
    }

    private GenApiResponse<SingleResponseEndpointWrapper> generateGenApiResponse(GenApiResponse<SingleResponseEndpoint> genApiResponse) {
        return GenApiResponse.<SingleResponseEndpointWrapper>builder()
                .statusCode(genApiResponse.getStatusCode())
                .message(genApiResponse.getMessage())
                .entity(
                        new SingleResponseEndpointWrapper(
                                jasyptIdEncryptor.encryptId(genApiResponse.getEntity().getId()),
                                genApiResponse.getEntity().getHttpMethod(),
                                genApiResponse.getEntity().getHttpHeaders(),
                                genApiResponse.getEntity().getResponse()
                        ))
                .build();
    }
}
