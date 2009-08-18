package org.opennms.sms.monitor;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.smslib.AGateway;
import org.springframework.beans.factory.FactoryBean;

public class PingTestGatewayFactoryBean implements FactoryBean {

	public Object getObject() throws Exception {
		return new GatewayGroup[] { new GatewayGroup(){

			public AGateway[] getGateways() {
				return new AGateway[] { new PingTestGateway("monkeys!") };
			}

			public String toString() {
				return "I am a monkey gateway!";
			}
		} };
	}

	public Class<?> getObjectType() {
		return GatewayGroup[].class;
	}

	public boolean isSingleton() {
		return true;
	}
	
	
}