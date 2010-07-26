package org.opennms.sms.gateways.internal;

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>OnmsGatewayGroupRegistrar class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsGatewayGroupRegistrar implements GatewayGroupRegistrar, DisposableBean, InitializingBean {
	
	private final List<Registration> m_registrations = new ArrayList<Registration>();
	
	private ServiceRegistry m_serviceRegistry;
	
	/** {@inheritDoc} */
	public void registerGatewayGroup(GatewayGroup gatewayGroup) {
		m_registrations.add( m_serviceRegistry.register(gatewayGroup, GatewayGroup.class));
	}

	/**
	 * <p>destroy</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public void destroy() throws Exception {
		for(Registration registration : m_registrations) {
            registration.unregister();
        }
	}

	/**
	 * <p>setServiceRegistry</p>
	 *
	 * @param serviceRegistry a {@link org.opennms.core.soa.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		m_serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>getServiceRegistry</p>
	 *
	 * @return a {@link org.opennms.core.soa.ServiceRegistry} object.
	 */
	public ServiceRegistry getServiceRegistry() {
		return m_serviceRegistry;
	}

	/**
	 * <p>afterPropertiesSet</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(m_serviceRegistry, "serviceRegistry must not be null");
	}

}
