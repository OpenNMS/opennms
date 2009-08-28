package org.opennms.sms.reflector.smsservice.internal;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.smslib.AGateway;
import org.smslib.test.*;

public class TestGatewayGroupImpl implements GatewayGroup {
	
	AGateway[] m_gateways;
	
	public TestGatewayGroupImpl(){
		m_gateways = new AGateway[1];
		m_gateways[0] = new TestGateway("modem.id");
	}
	
	public AGateway[] getGateways() {
		return m_gateways;
	}
	
}
