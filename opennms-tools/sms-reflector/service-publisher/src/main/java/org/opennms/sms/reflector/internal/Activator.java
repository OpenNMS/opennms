package org.opennms.sms.reflector.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.smslib.Service;

public class Activator implements BundleActivator {
	
	private ServiceRegistration m_registration;
	
	public void start(BundleContext context) throws Exception {
		m_registration = context.registerService(Service.class.getName(), new Service(), null);
	}

	public void stop(BundleContext context) throws Exception {
		m_registration.unregister();
        m_registration = null;
	}

}
