package org.opennms.sms.reflector.smsservice.internal;

import java.util.HashMap;
import java.util.Map;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class OnmsSmsServiceRegistrar implements SmsServiceRegistrar, InitializingBean {
	
	private ServiceRegistry m_serviceRegistry;
	private Map<SmsService, Registration> m_registrationMap = new HashMap<SmsService, Registration>();
	
	public void registerSmsService(SmsService service) {
		Registration registration = getServiceRegistry().register(service, SmsService.class);
		m_registrationMap.put(service, registration);
	}
	
	public void unregisterSmsService(SmsService service) {
	    Registration registration = m_registrationMap.remove(service);
	    if (registration != null) {
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
