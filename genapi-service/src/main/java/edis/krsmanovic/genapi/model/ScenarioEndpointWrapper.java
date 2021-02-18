package edis.krsmanovic.genapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScenarioEndpointWrapper {
    private String id;
    private HttpMethod httpMethod;
    private List<HttpHeader> httpHeaders;
    private List<EndpointResponse> endpointResponses;
}
