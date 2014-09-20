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
	private final List<ServiceRegistration<GatewayGroup>> m_registrations = new ArrayList<ServiceRegistration<GatewayGroup>>();

	/** {@inheritDoc} */
        @Override
	public void registerGatewayGroup( GatewayGroup gatewayGroup ) {
		m_registrations.add(m_context.registerService(GatewayGroup.class, gatewayGroup, null));
	}

	/** {@inheritDoc} */
        @Override
	public void setBundleContext( BundleContext bundleContext ) {
		m_context = bundleContext;
		
	}

	/**
	 * <p>destroy</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
        @Override
	public void destroy() throws Exception {
		for(ServiceRegistration<GatewayGroup> registration : m_registrations) {
            registration.unregister();
        }
	}
}
