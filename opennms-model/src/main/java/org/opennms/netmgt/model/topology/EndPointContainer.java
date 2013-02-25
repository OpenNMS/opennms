package org.opennms.netmgt.model.topology;


/**
 * This class represents a destination in the network such as
 * an IP address or a physical port.
 * 
 * @author Antonio
 *
 */
public abstract class EndPointContainer extends EndPoint{
	
	private EndPoint m_encapsulatedby;

	public EndPoint getEncapsulatedBy() {
		return m_encapsulatedby;
	}

	public void setEncapsulatedBy(EndPoint encapsulatedby) {
		m_encapsulatedby = m_encapsulatedby;
	}
	
	
}
