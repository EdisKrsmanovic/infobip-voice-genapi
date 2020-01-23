package org.infobip.voice.genapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class EndpointResponse implements Serializable {

    @JsonStringConstraint
    private String body;
    private Integer ordinalNumber;

    public EndpointResponse(String body) {
        this.body = body;
    }
}