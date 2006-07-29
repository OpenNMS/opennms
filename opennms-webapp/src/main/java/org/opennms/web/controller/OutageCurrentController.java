package org.opennms.web.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.svclayer.OutageService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

//import org.springframework.web.servlet.mvc.SimpleFormController;
//public class OutageController extends SimpleFormController {
public class OutageCurrentController extends AbstractController {

	OutageService m_outageService;

	Collection<OnmsOutage> outages;

	// private OutageService outageService;
	// BEAN Setter

	public void setOutageService(OutageService service) {
		m_outageService = service;
	}

	// public Map referenceData(HttpServletRequest request) throws Exception {
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {

		Integer offset = 1;
		Integer limit = 50;

		outages = m_outageService.getCurrenOutagesByRange(offset, limit);

		Map myModel = new HashMap();
		String now = (new java.util.Date()).toString();

		myModel.put("now", now);
		myModel.put("outages", outages);

		return new ModelAndView("displayCurrentOutages", myModel);
	}

}
