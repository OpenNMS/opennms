package org.opennms.web.controller;

import java.util.Map;
import org.opennms.netmgt.dao.ServiceInfo;

public class DaemonStatusBinder {
	private String[] values = new String[0];
	
	public void setValues(String[] values) {
		this.values = values;
	}
	
	public String[] getValues() {
		return values;
	}
	
}
