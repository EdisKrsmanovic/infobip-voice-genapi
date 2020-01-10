package org.infobip.voice.genapi.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.infobip.voice.genapi.model.Endpoint;
import org.infobip.voice.genapi.model.JsonStringConstraint;
import org.infobip.voice.genapi.model.ScenarioEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.*;
import java.io.IOException;
import java.util.Set;

@Component
@Validated
public class EndpointValidator implements ConstraintValidator<JsonStringConstraint, String> {

    @Autowired
    private Validator validator;

    @Override
    public void initialize(JsonStringConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String body, ConstraintValidatorContext constraintValidatorContext) {
        return (isJSONValid(body));
    }

    public boolean checkIfValid(@Valid Endpoint endpoint) {
        return endpoint.getResponse() == null || isJSONValid(endpoint.getResponse().getBody());
    }

    //@Valid will check every response
    public boolean checkIfValidScenario(@Valid ScenarioEndpoint scenarioEndpoint) {
        return true;
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
