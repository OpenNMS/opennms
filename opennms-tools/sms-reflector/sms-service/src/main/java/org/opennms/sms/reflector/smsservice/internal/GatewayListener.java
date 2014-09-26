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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.Service;
import org.smslib.Service.ServiceStatus;

/**
 * <p>GatewayListener class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GatewayListener {
    private static final Logger LOG = LoggerFactory.getLogger(GatewayListener.class);
	
	private Service m_service;
	
	
	/**
	 * <p>setService</p>
	 *
	 * @param service a {@link org.smslib.Service} object.
	 */
	public void setService(Service service){
		m_service = service;
	}
	
	/**
	 * <p>addGateway</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @param properties a {@link java.util.Map} object.
	 */
    public void addGateway(AGateway gateway, Map<?,?> properties){
		if(m_service != null){
			try{
				if(m_service.getServiceStatus() == ServiceStatus.STARTED){
					m_service.stopService();
				}
		        
				m_service.addGateway(gateway);
				m_service.startService();
				
			}catch(final Exception e){
			    LOG.warn("Unable to add gateway ({}) to SMS service", gateway, e);
			}
			
		}
	}
	
	/**
	 * <p>removeGateway</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @param properties a {@link java.util.Map} object.
	 */
    public void removeGateway(AGateway gateway, Map<?,?> properties){
		if(m_service != null){
			try{
				if(m_service.getServiceStatus() == ServiceStatus.STARTED){
					m_service.stopService();
				}
				m_service.removeGateway(gateway);
				m_service.startService();
			}catch(final Exception e){
			    LOG.warn("Unable to remove gateway ({}) from SMS service", gateway, e);
			}
			
		}
	}

	/**
	 * <p>bind</p>
	 *
	 * @param arg0 a {@link java.lang.Object} object.
	 * @param arg1 a {@link java.util.Map} object.
	 * @throws java.lang.Exception if any.
	 */
    public void bind(Object arg0, Map<?,?> arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>unbind</p>
	 *
	 * @param arg0 a {@link java.lang.Object} object.
	 * @param arg1 a {@link java.util.Map} object.
	 * @throws java.lang.Exception if any.
	 */
    public void unbind(Object arg0, Map<?,?> arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
