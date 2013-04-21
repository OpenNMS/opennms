package org.opennms.netmgt.model.topology;

import java.util.List;


/**
 * This class is a container of end points.  In the network, this
 * can be either a physical or virtual node/device/subnetwork/etc.
 */
public abstract class Element {

	private List<EndPoint> m_endpoints;

	public List<EndPoint> getEndpoints() {
		return m_endpoints;
	}

	public void setEndpoints(List<EndPoint> endpoints) {
		m_endpoints = endpoints;
	}

}
