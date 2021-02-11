package org.infobip.voice.genapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ScenarioEndpoint implements Endpoint, Serializable {

    @NotNull(groups = UpdateValidation.class, message = "Cannot update Scenario Endpoint with null id")
    private Integer id;
    @NotNull(message = "HttpMethod cannot be null")
    private HttpMethod httpMethod;
    @Valid
    @NotNull(message = "HttpHeaders cannot be null, set it to empty array if you do not want to specify headers now")
    private List<HttpHeader> httpHeaders;
    @Valid
    @NotNull(message = "EndpointResponses cannot be null, set it to empty array if you do not want to specify responses now")
    private List<EndpointResponse> endpointResponses;

    private AtomicInteger nextResponseNo = new AtomicInteger(0);
    private LocalTime responseFirstAccessTime = LocalTime.now();

    public ScenarioEndpoint(Integer id, @NotNull(message = "HttpMethod cannot be null") HttpMethod httpMethod, @Valid List<HttpHeader> httpHeaders, @Valid List<EndpointResponse> endpointResponses) {
        this.id = id;
        this.httpMethod = httpMethod;
        this.httpHeaders = httpHeaders;
        this.endpointResponses = endpointResponses;
    }

    public interface UpdateValidation {
    }
}