package org.infobip.voice.genapi.connector.model;

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
    private List<HttpHeader> httpHeaders;
    @Valid
    private List<EndpointResponse> endpointResponses;

    private AtomicInteger nextResponseNo;
    private LocalTime responseFirstAccessTime;

    public ScenarioEndpoint(Integer id, @NotNull(message = "HttpMethod cannot be null") HttpMethod httpMethod, @Valid List<HttpHeader> httpHeaders, @Valid List<EndpointResponse> endpointResponses) {
        this.id = id;
        this.httpMethod = httpMethod;
        this.httpHeaders = httpHeaders;
        this.endpointResponses = endpointResponses;

        this.nextResponseNo = new AtomicInteger(0);
        this.responseFirstAccessTime = LocalTime.now();
    }

    @Override
    public EndpointResponse getResponse() {
        if(endpointResponses.isEmpty()) return null;
        EndpointResponse endpointResponse = endpointResponses.get(nextResponseNo.getAndIncrement());
        nextResponseNo.set(nextResponseNo.intValue() % endpointResponses.size());
        return endpointResponse;
    }

    public interface UpdateValidation {
    }
}