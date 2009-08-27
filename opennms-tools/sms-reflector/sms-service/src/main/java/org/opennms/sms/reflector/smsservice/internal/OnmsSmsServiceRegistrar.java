package org.opennms.sms.reflector.smsservice.internal;

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class OnmsSmsServiceRegistrar implements SmsServiceRegistrar, InitializingBean, DisposableBean {
	
	private ServiceRegistry m_serviceRegistry;
	private List<Registration> m_registrations = new ArrayList<Registration>();
	
	public void registerSmsService(SmsService service) {
		m_registrations.add(getServiceRegistry().register(service, SmsService.class));
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
		Assert.notNull(m_serviceRegistry);
	}

}
