package gr.dkateros.springboot.backendtest.entity.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.validator.routines.DomainValidator;

/**
 * Delegate to apache commons implementation, which is compliant with the relevant RFCs.
 */
public class HostnameValidator 
implements ConstraintValidator<Hostname, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		DomainValidator validator = DomainValidator.getInstance(true);
		return validator.isValid(value);
	}
	
}
