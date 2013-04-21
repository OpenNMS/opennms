package org.opennms.netmgt.model.topology;

import java.util.List;


/**
 * A collection of End Points providing a network based service.
 * For example, this can be used to represent a subnetwork since
 * a subnetwork is a collection of IP addressed end points.
 */
public abstract class Service {

	private List<EndPoint> m_endpoints;
	
	private String m_name;

	public List<EndPoint> getEndpoints() {
		return m_endpoints;
	}

	public void setEndpoints(List<EndPoint> endpoints) {
		m_endpoints = endpoints;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

}
