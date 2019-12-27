package org.infobip.voice.genapi.model;

import org.infobip.voice.genapi.validator.SingleResponseEndpointValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SingleResponseEndpointValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonStringConstraint {
    String message() default "Body has invalid json format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
