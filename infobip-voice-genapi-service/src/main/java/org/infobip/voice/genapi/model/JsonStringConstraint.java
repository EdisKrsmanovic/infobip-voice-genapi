package org.infobip.voice.genapi.model;

import org.infobip.voice.genapi.validator.EndpointValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EndpointValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonStringConstraint {
    String message() default "Response has invalid body json format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
