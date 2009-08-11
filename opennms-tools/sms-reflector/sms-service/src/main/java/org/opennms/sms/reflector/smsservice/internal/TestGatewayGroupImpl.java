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
	
	public void stopGateways(){
		System.out.printf("%s stopping all gateways in group %s", "\n", "\n");
		for(AGateway gateway : m_gateways){
			try {
				if(gateway.getStatus() != AGateway.GatewayStatuses.STOPPED){
					gateway.stopGateway();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
