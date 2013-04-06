package org.opennms.netmgt.model.topology;

import java.util.List;


/**
 * This class represents a destination in the network such as
 * an IP address or a physical port.
 * 
 * @author Antonio
 *
 */
public abstract class Encapsulable extends EndPoint {
	
	private List<EndPoint> m_encapsulatedby;

	public List<EndPoint> getEncapsulatedBy() {
		return m_encapsulatedby;
	}

	public void setEncapsulatedBy(EndPoint encapsulatedby) {
		m_encapsulatedby.add(encapsulatedby);
	}
	
	
}
