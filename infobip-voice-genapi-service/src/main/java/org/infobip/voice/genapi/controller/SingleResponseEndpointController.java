package org.infobip.voice.genapi.controller;

import lombok.AllArgsConstructor;
import org.infobip.voice.genapi.encryptor.JasyptIdEncryptor;
import org.infobip.voice.genapi.exception.InvalidIdException;
import org.infobip.voice.genapi.model.GenApiResponse;
import org.infobip.voice.genapi.model.SingleResponseEndpoint;
import org.infobip.voice.genapi.model.SingleResponseEndpointWrapper;
import org.infobip.voice.genapi.service.SingleResponseEndpointService;
import org.springframework.web.bind.annotation.*;

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
