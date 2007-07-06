package org.opennms.web.map;

import javax.servlet.http.HttpServletRequest;
import org.springframework.validation.Errors;

public class SaveMapValidator extends MapApplianceValidator {
	public boolean supports(Class aClass) {
		return aClass.equals(HttpServletRequest.class);
	}

	public void validate(Object o, Errors errors) {
		
		super.validate(o, errors);
		
		if (!action.equals(MapsConstants.SAVEMAP_ACTION) ) 
			errors.rejectValue("Action", MapsConstants.SAVEMAP_ACTION+"Failed" , null, "action should be " + MapsConstants.SAVEMAP_ACTION); 
		String mapIdentificator = request.getParameter("MapId");
		if (mapIdentificator == null) 
			errors.rejectValue("MapId", MapsConstants.OPENMAP_ACTION+"Failed" , null, "HttpServletReqiest parameter MapId is required");
		
		try {
			Integer.parseInt(mapIdentificator) ;
		} catch (NumberFormatException e) {
			errors.rejectValue("MapId", MapsConstants.OPENMAP_ACTION+"Failed" , null, "MapId is not an Integer");
		}
			
	}
}
