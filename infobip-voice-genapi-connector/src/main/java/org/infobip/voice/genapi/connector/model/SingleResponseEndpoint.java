package org.infobip.voice.genapi.connector.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.infobip.voice.genapi.connector.model.Endpoint;
import org.infobip.voice.genapi.connector.model.HttpHeader;
import org.infobip.voice.genapi.connector.model.EndpointResponse;
import org.springframework.http.HttpMethod;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SingleResponseEndpoint implements Endpoint, Serializable {

    @NotNull(groups = UpdateValidation.class, message = "Cannot update Scenario Endpoint with null id")
    private Integer id;
    @NotNull(message = "HttpMethod cannot be null")
    private HttpMethod httpMethod;
    @Valid
    private List<HttpHeader> httpHeaders;
    private EndpointResponse response;

    public SingleResponseEndpoint(Integer id, @NotNull(message = "HttpMethod cannot be null") HttpMethod httpMethod, @Valid List<HttpHeader> httpHeaders, String responseBody) {
        this.id = id;
        this.httpMethod = httpMethod;
        this.httpHeaders = httpHeaders;
        this.response = new EndpointResponse(responseBody);
    }


    public interface UpdateValidation {
    }
}
