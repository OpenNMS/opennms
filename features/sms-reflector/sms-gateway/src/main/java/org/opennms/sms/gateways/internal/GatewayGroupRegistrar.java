package org.opennms.sms.gateways.internal;

import org.opennms.sms.reflector.smsservice.GatewayGroup;

/**
 * <p>GatewayGroupRegistrar interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface GatewayGroupRegistrar {
	
	/**
	 * <p>registerGatewayGroup</p>
	 *
	 * @param gatewayGroup a {@link org.opennms.sms.reflector.smsservice.GatewayGroup} object.
	 */
	public void registerGatewayGroup(GatewayGroup gatewayGroup);
	
}
