package org.opennms.netmgt.threshd;

import java.util.Map;

import org.opennms.netmgt.utils.ParameterMap;

public class LatencyParameters {

	private Map m_parameters;
	private String m_svcName;

	public LatencyParameters(Map parameters, String svcName) {

		m_parameters = parameters;
		m_svcName = svcName;
	}

	public Map getParameters() {
		return m_parameters;
	}
	

	public String getServiceName() {
		return m_svcName;
	}

	int getInterval() {
		Map parameters = getParameters();
	    int interval = ParameterMap.getKeyedInteger(parameters, "interval", LatencyThresholder.DEFAULT_INTERVAL);
	    return interval;
	}

	String getGroupName() {
		Map parameters = getParameters();
	    String groupName = ParameterMap.getKeyedString(parameters, "thresholding-group", "default");
	    return groupName;
	}
	
	int getRange() {
		Map parameters = getParameters();
	    int range = ParameterMap.getKeyedInteger(parameters, "range", LatencyThresholder.DEFAULT_RANGE);
	    return range;
	}
	

}
