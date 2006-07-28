package org.opennms.web.controller;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.svclayer.catstatus.model.StatusSection;
import org.opennms.web.svclayer.catstatus.support.DefaultCategoryStatusService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class CategoryStatusController extends AbstractController {

	DefaultCategoryStatusService m_categorystatusservice;
	Collection <StatusSection>statusSections;
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
		
		statusSections = m_categorystatusservice.getCategoriesStatus(); 
		ModelAndView modelAndView = new ModelAndView("displayCategoryStatus","statusTree",statusSections);
		
		return modelAndView;
	}

	
	public void setCategoryStatusService(DefaultCategoryStatusService categoryStatusService){
		
		m_categorystatusservice = categoryStatusService;
		
		
	}
	
}
