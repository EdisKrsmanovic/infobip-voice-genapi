package edis.krsmanovic.genapi.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import edis.krsmanovic.genapi.model.Endpoint;
import edis.krsmanovic.genapi.model.ScenarioEndpoint;
import edis.krsmanovic.genapi.model.SingleResponseEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.*;
import java.io.IOException;
import java.util.Set;

@Component
@Validated
public class EndpointValidator {

    @Autowired
    private Validator validator;

    public boolean checkIfValid(@Valid SingleResponseEndpoint singleResponseEndpoint) {
        return singleResponseEndpoint.getResponse() == null || isJSONValid(singleResponseEndpoint.getResponse().getBody());
    }

    public boolean checkIfValidScenario(@Valid ScenarioEndpoint scenarioEndpoint) {
        return scenarioEndpoint.getEndpointResponses().stream().allMatch(e -> isJSONValid(e.getBody())) || scenarioEndpoint.getEndpointResponses().size() == 0;
    }

    public void validate(Endpoint endpoint, Class<?>... groups) {
        Set<ConstraintViolation<Object>> violations = validator.validate(endpoint, groups);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    public boolean isJSONValid(String jsonInString) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
