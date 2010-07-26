package org.opennms.sms.gateways.internal;

import java.util.ArrayList;
import java.util.List;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.osgi.context.BundleContextAware;

/**
 * <p>OsgiGatewayGroupRegistrar class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OsgiGatewayGroupRegistrar implements GatewayGroupRegistrar, BundleContextAware, DisposableBean {
	
	private BundleContext m_context;
	private final List<ServiceRegistration> m_registrations = new ArrayList<ServiceRegistration>();

	/** {@inheritDoc} */
	public void registerGatewayGroup( GatewayGroup gatewayGroup ) {
		m_registrations.add(m_context.registerService(GatewayGroup.class.getName(), gatewayGroup, null));
	}

	/** {@inheritDoc} */
	public void setBundleContext( BundleContext bundleContext ) {
		m_context = bundleContext;
		
	}

	/**
	 * <p>destroy</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public void destroy() throws Exception {
		for(ServiceRegistration registration : m_registrations) {
            registration.unregister();
        }
	}
}
