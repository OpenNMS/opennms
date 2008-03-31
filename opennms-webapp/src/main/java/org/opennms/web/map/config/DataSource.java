package org.opennms.web.map.config;

import java.util.HashMap;

public class DataSource {
	String label;
	String implClass;
	HashMap param;
	Filter[] filters;
	

	public DataSource(String label, String implClass, HashMap param, Filter[] filters) {
		super();
		this.label=label;
		this.implClass = implClass;
		this.param = param;
		this.filters = filters;
	}
	
	public DataSource(){
		
	}

	public Filter[] getFilters() {
		return filters;
	}

	public void setFilters(Filter[] filters) {
		this.filters = filters;
	}

	public String getImplClass() {
		return implClass;
	}

	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}

	public HashMap getParam() {
		return param;
	}

	public void setParam(HashMap param) {
		this.param = param;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	
	
}
