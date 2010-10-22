package org.opennms.sms.reflector.commands.internal;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.smslib.AGateway;

/**
 * <p>GatewayGroupImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GatewayGroupImpl implements GatewayGroup {
	
	private AGateway[] m_gateways;
	
	/**
	 * <p>setGateways</p>
	 *
	 * @param gateways an array of {@link org.smslib.AGateway} objects.
	 */
	public void setGateways(AGateway[] gateways){
		m_gateways = gateways;
	}
	
	/**
	 * <p>getGateways</p>
	 *
	 * @return an array of {@link org.smslib.AGateway} objects.
	 */
	public AGateway[] getGateways() {
		return m_gateways;
	}

}
