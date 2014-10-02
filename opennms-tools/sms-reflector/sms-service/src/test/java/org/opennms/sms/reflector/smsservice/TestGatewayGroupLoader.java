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

package org.opennms.sms.reflector.smsservice;

import org.opennms.core.soa.ServiceRegistry;
import org.smslib.AGateway;
import org.smslib.test.TestGateway;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class TestGatewayGroupLoader implements InitializingBean {
	
	private ServiceRegistry m_serviceRegistry;
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		m_serviceRegistry = serviceRegistry;
	}

	public ServiceRegistry getServiceRegistry() {
		return m_serviceRegistry;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(m_serviceRegistry, "serviceRegistry must not be null");
		
		GatewayGroup g = new GatewayGroup(){
                        @Override
			public AGateway[] getGateways() {
				return new AGateway[] { new TestGateway("ACM0") };
			}
			
                        @Override
			public String toString() {
				return "I am a monkey gateway!";
			}
		};
		
		m_serviceRegistry.register( g, GatewayGroup.class);
	}
	
	
}