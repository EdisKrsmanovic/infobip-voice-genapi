package org.infobip.voice.model;

import org.infobip.voice.validator.HttpEndpointValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = HttpEndpointValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonStringConstraint {
    String message() default "Body has invalid json format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
