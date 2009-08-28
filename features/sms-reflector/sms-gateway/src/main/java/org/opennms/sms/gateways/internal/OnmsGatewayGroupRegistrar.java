package org.opennms.sms.gateways.internal;

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class OnmsGatewayGroupRegistrar implements GatewayGroupRegistrar, DisposableBean, InitializingBean {
	
	private final List<Registration> m_registrations = new ArrayList<Registration>();
	
	private ServiceRegistry m_serviceRegistry;
	
	public void registerGatewayGroup(GatewayGroup gatewayGroup) {
		m_registrations.add( m_serviceRegistry.register(gatewayGroup, GatewayGroup.class));
	}

	public void destroy() throws Exception {
		for(Registration registration : m_registrations) {
            registration.unregister();
        }
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		m_serviceRegistry = serviceRegistry;
	}

	public ServiceRegistry getServiceRegistry() {
		return m_serviceRegistry;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(m_serviceRegistry, "serviceRegistry must not be null");
	}

}
