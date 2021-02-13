package org.infobip.voice.genapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SingleResponseEndpointWrapper {
    private String id;
    private HttpMethod httpMethod;
    private List<HttpHeader> httpHeaders;
    private EndpointResponse response;
}
