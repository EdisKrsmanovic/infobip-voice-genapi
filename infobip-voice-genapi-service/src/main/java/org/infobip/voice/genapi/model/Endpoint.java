package org.infobip.voice.genapi.model;

import org.springframework.http.HttpMethod;

import java.util.List;

public interface Endpoint {
    Integer getId();
    HttpMethod getHttpMethod();
    List<HttpHeader> getHttpHeaders();
    EndpointResponse getResponse();
    void setId(Integer id);
}
