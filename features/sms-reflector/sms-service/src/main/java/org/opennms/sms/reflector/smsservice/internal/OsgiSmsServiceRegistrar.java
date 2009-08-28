package org.opennms.sms.reflector.smsservice.internal;

import java.util.HashMap;
import java.util.Map;

import org.opennms.sms.reflector.smsservice.SmsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.BundleContextAware;

public class OsgiSmsServiceRegistrar implements SmsServiceRegistrar, BundleContextAware {

	private BundleContext m_bundleContext;
	private Map<SmsService, ServiceRegistration> m_registrationMap = new HashMap<SmsService, ServiceRegistration>();

	public void registerSmsService(SmsService service) {
	    ServiceRegistration registration = m_bundleContext.registerService(SmsService.class.getName(), service, null);
	    m_registrationMap.put(service, registration);
	}
	
	public void unregisterSmsService(SmsService smsService) {
	    ServiceRegistration registration = m_registrationMap.remove(smsService);
	    registration.unregister();
	}
	
	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
		
	}


}
