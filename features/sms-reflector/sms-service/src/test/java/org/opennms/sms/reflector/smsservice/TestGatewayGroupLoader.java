package org.opennms.sms.reflector.smsservice;

import org.opennms.core.soa.ServiceRegistry;
import org.smslib.AGateway;
import org.smslib.test.TestGateway;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class TestGatewayGroupLoader implements InitializingBean {
	
	private ServiceRegistry m_serviceRegistry;
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		m_serviceRegistry = serviceRegistry;
	}

	public ServiceRegistry getServiceRegistry() {
		return m_serviceRegistry;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(m_serviceRegistry, "serviceRegistry must not be null");
		
		GatewayGroup g = new GatewayGroup(){
			public AGateway[] getGateways() {
				return new AGateway[] { new TestGateway("ACM0") };
			}
			
			public String toString() {
				return "I am a monkey gateway!";
			}
		};
		
		m_serviceRegistry.register( g, GatewayGroup.class);
	}
	
	
}