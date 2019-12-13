package org.infobip.voice.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.infobip.voice.model.JsonStringConstraint;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.IOException;

@Component
public class HttpEndpointValidator implements ConstraintValidator<JsonStringConstraint, String> {
    @Override
    public void initialize(JsonStringConstraint constraintAnnotation) {

    }

    @Override
    public boolean isValid(String body, ConstraintValidatorContext constraintValidatorContext) {
        return(isJSONValid(body));
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
