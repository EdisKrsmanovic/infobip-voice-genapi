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
public class HttpEndpoint {
    private Integer id;
    @NotNull(message = "HttpMethod cannot be null")
    private HttpMethod httpMethod;
    @Valid
    private List<HttpHeader> httpHeaders;
    @JsonStringConstraint
    private String body;
}
