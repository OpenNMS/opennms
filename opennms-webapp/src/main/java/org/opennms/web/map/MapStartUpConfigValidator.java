package org.opennms.web.map;


import org.opennms.web.map.config.MapStartUpConfig;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class MapStartUpConfigValidator implements Validator{
	public boolean supports(Class aClass) {
		return aClass.equals(MapStartUpConfig.class);
	}

	public void validate(Object o, Errors errors) {
		
		MapStartUpConfig config = (MapStartUpConfig) o;
		if (config == null ) {
			errors.rejectValue("refreshTime", "Error!", null, " map start Up Configuration is null");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "user", "field.required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "screenWidth", "field.required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "screenHeight", "field.required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "refreshTime", "field.required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "fullScreen", "field.required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mapToOpenId", "field.required");
		}
	
	}
}
