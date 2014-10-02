/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.reflector.smsservice.internal;

import java.util.HashMap;
import java.util.Map;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>OnmsSmsServiceRegistrar class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsSmsServiceRegistrar implements SmsServiceRegistrar, InitializingBean {
	
	private ServiceRegistry m_serviceRegistry;
	private Map<SmsService, Registration> m_registrationMap = new HashMap<SmsService, Registration>();
	
	/** {@inheritDoc} */
        @Override
	public void registerSmsService(SmsService service) {
		Registration registration = getServiceRegistry().register(service, SmsService.class);
		m_registrationMap.put(service, registration);
	}
	
	/** {@inheritDoc} */
        @Override
	public void unregisterSmsService(SmsService service) {
	    Registration registration = m_registrationMap.remove(service);
	    if (registration != null) {
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
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(m_serviceRegistry);
	}

}
