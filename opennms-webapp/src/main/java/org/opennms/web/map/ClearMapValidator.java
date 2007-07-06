package org.opennms.web.map;

import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.Errors;

public class ClearMapValidator extends MapApplianceValidator {
	public boolean supports(Class aClass) {
		return aClass.equals(HttpServletRequest.class);
	}

	public void validate(Object o, Errors errors) {
		
		super.validate(o, errors);
		
		if (!action.equals(MapsConstants.CLEAR_ACTION) ) 
			errors.rejectValue("Action", MapsConstants.CLEAR_ACTION+"Failed" , null, "action should be " + MapsConstants.CLEAR_ACTION); 
	}
}
