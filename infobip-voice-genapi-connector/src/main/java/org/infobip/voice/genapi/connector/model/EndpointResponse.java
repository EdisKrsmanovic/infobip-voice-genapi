package org.infobip.voice.genapi.connector.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class EndpointResponse implements Serializable {
    private String body;
}
