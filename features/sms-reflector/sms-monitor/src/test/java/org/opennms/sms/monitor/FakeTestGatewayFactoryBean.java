package org.opennms.sms.monitor;

import org.opennms.core.soa.ServiceRegistry;
import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.smslib.AGateway;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class FakeTestGatewayFactoryBean implements InitializingBean {
	
	
	private ServiceRegistry m_serviceRegistry;

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(getServiceRegistry(), "serviceRegistry must not be null");
		
		GatewayGroup gatewayGroup = new GatewayGroup() {

			public AGateway[] getGateways() {
				System.err.println("getting ACM0");
				return new AGateway[] { new FakeTestGateway( "ACM0" ) };
			}
			
		};
		
		getServiceRegistry().register(gatewayGroup, GatewayGroup.class);
		
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		m_serviceRegistry = serviceRegistry;
	}

	public ServiceRegistry getServiceRegistry() {
		return m_serviceRegistry;
	}
	
	
}