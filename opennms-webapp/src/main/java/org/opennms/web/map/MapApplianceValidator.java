package org.opennms.web.map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.opennms.web.map.view.Manager;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class MapApplianceValidator implements Validator {
	
	public boolean supports(Class aClass) {
		return aClass.equals(HttpServletRequest.class);
	}

	String action;
	
	HttpServletRequest request;
	
	public void validate(Object o, Errors errors) {
		
		request = (HttpServletRequest) o;
		
		action = request.getParameter("action");
		

		HttpSession session = request.getSession(false);
		if (session == null) 
			errors.rejectValue("Session", action+"Failed" , null, "Http Session is null");
		Manager m = null;
		m = (Manager) session.getAttribute("manager");
		if (m == null ) 
			errors.rejectValue("Manager", action+"Failed" , null, "Http Session Map Manager is null");

	}
}
