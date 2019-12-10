package org.infobip.voice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HttpEndpoint implements Serializable {
    private Integer id;
    private HttpMethod httpMethod;
    private List<HttpHeader> httpHeaders;
    private String body;
}
