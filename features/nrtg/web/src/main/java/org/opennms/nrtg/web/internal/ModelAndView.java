package org.opennms.nrtg.web.internal;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModelAndView {
	
	private final String m_viewName;
	private final Map<String, Object> m_model;

	public ModelAndView(String viewName) {
		m_viewName = viewName;
		m_model = new LinkedHashMap<String, Object>();
	}

	public void addObject(String name, Object modelObject) {
		m_model.put(name, modelObject);
	}
	
	public String getViewName() {
		return m_viewName;
	}
	
	public Map<String, Object> getModel() {
		return m_model;
	}

}
