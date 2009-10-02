package org.opennms.sms.reflector.smsservice;

import org.smslib.AGateway;

public interface GatewayGroup {
	AGateway[] getGateways();
}
