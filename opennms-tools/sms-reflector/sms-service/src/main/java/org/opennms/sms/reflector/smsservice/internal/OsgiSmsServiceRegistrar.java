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

import org.opennms.sms.reflector.smsservice.SmsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.BundleContextAware;

/**
 * <p>OsgiSmsServiceRegistrar class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OsgiSmsServiceRegistrar implements SmsServiceRegistrar, BundleContextAware {

	private BundleContext m_bundleContext;
	private Map<SmsService, ServiceRegistration<SmsService>> m_registrationMap = new HashMap<SmsService, ServiceRegistration<SmsService>>();

	/** {@inheritDoc} */
        @Override
	public void registerSmsService(SmsService service) {
	    ServiceRegistration<SmsService> registration = m_bundleContext.registerService(SmsService.class, service, null);
	    m_registrationMap.put(service, registration);
	}
	
	/** {@inheritDoc} */
        @Override
	public void unregisterSmsService(SmsService smsService) {
	    ServiceRegistration<SmsService> registration = m_registrationMap.remove(smsService);
	    registration.unregister();
	}
	
	/** {@inheritDoc} */
        @Override
	public void setBundleContext(BundleContext bundleContext) {
		m_bundleContext = bundleContext;
		
	}


}
