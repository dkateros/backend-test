package gr.dkateros.springboot.backendtest.entity.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = IPv4Validator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface IPv4Address {
	String message() default "Invalid IPv4 address";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
