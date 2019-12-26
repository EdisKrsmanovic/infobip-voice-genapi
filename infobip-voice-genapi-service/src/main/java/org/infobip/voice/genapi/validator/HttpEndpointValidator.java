package org.infobip.voice.genapi.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.infobip.voice.genapi.model.HttpEndpoint;
import org.infobip.voice.genapi.model.JsonStringConstraint;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import java.io.IOException;

@Component
@Validated
public class HttpEndpointValidator implements ConstraintValidator<JsonStringConstraint, String> {
    @Override
    public void initialize(JsonStringConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String body, ConstraintValidatorContext constraintValidatorContext) {
        return(isJSONValid(body));
    }

    public boolean checkIfValid(@Valid HttpEndpoint httpEndpoint) {
        return(isJSONValid(httpEndpoint.getBody()));
    }

    private boolean isJSONValid(String jsonInString ) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
