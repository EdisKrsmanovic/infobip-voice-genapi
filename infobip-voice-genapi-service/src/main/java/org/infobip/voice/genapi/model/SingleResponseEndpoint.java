package org.infobip.voice.genapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Validated
public class SingleResponseEndpoint implements Endpoint {

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
