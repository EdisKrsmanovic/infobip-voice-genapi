package org.infobip.voice.genapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointResponse implements Serializable {

    private String body;
    private Integer ordinalNumber;

    public EndpointResponse(String body) {
        this.body = body;
    }
}