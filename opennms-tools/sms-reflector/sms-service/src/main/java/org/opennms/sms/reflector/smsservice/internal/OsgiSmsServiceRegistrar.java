package org.opennms.sms.reflector.smsservice.internal;

import java.util.ArrayList;
import java.util.List;

import org.opennms.sms.reflector.smsservice.SmsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.osgi.context.BundleContextAware;

public class OsgiSmsServiceRegistrar implements SmsServiceRegistrar, BundleContextAware, DisposableBean {

	private BundleContext m_bundleContext;
	private List<ServiceRegistration> m_registrations = new ArrayList<ServiceRegistration>();

	public void registerSmsService(SmsService service) {
		m_registrations.add(m_bundleContext.registerService(SmsService.class.getName(), service, null));
	}

	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
		
	}

	public void destroy() throws Exception {
		for(ServiceRegistration registration : m_registrations){
			registration.unregister();
		}
		
	}

}
