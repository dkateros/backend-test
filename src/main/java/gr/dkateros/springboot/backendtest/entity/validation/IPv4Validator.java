package gr.dkateros.springboot.backendtest.entity.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Delegate to apache commons implementation.
 */
public class IPv4Validator 
implements ConstraintValidator<IPv4Address, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		InetAddressValidator validator = InetAddressValidator.getInstance();
		return validator.isValidInet4Address(value);
	}

}