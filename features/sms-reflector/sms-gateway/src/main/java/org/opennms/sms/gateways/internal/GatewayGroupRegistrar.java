package org.opennms.sms.gateways.internal;

import org.opennms.sms.reflector.smsservice.GatewayGroup;

public interface GatewayGroupRegistrar {
	
	public void registerGatewayGroup(GatewayGroup gatewayGroup);
	
}
