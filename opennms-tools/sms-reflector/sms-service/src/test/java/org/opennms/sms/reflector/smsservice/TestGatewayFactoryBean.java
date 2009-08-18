package org.opennms.sms.reflector.smsservice;

import org.smslib.AGateway;
import org.smslib.test.TestGateway;
import org.springframework.beans.factory.FactoryBean;

public class TestGatewayFactoryBean implements FactoryBean {

	public Object getObject() throws Exception {
		return new GatewayGroup[] { new GatewayGroup(){

			public AGateway[] getGateways() {
				return new AGateway[] { new TestGateway("monkeys!") };
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