package org.opennms.sms.reflector.commands.internal;

import java.util.ArrayList;
import java.util.List;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.smslib.AGateway;

public class GatewayGroupImpl implements GatewayGroup {
	
	private AGateway[] m_gateways;
	
	public void setGateways(AGateway[] gateways){
		m_gateways = gateways;
	}
	
	public AGateway[] getGateways() {
		return m_gateways;
	}

}
