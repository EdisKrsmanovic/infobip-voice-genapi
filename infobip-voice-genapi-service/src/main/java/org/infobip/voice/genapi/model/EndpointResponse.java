package org.infobip.voice.genapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EndpointResponse {

    @JsonStringConstraint
    private String body;
    private Integer ordinalNumber;

    public EndpointResponse(String body) {
        this.body = body;
    }
}