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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.Service.ServiceStatus;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>SmsServiceListFactoryBean class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SmsServiceListFactoryBean implements FactoryBean<SmsService[]>, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(SmsServiceListFactoryBean.class);
	private GatewayGroup[] m_gatewayGroups;
	private SmsService[] m_services;
	
	/**
	 * <p>setOutboundMessageNotification</p>
	 *
	 * @param mOutboundMessageNotification a {@link org.smslib.IOutboundMessageNotification} object.
	 */
	public void setOutboundMessageNotification(IOutboundMessageNotification mOutboundMessageNotification) {
		m_outboundMessageNotification = mOutboundMessageNotification;
	}

	/**
	 * <p>setInboundMessageNotification</p>
	 *
	 * @param mInboundMessageNotification a {@link org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification} object.
	 */
	public void setInboundMessageNotification(OnmsInboundMessageNotification mInboundMessageNotification) {
		m_inboundMessageNotification = mInboundMessageNotification;
	}

	/**
	 * <p>setGatewayStatusNotification</p>
	 *
	 * @param mGatewayStatusNotification a {@link org.smslib.IGatewayStatusNotification} object.
	 */
	public void setGatewayStatusNotification(IGatewayStatusNotification mGatewayStatusNotification) {
		m_gatewayStatusNotification = mGatewayStatusNotification;
	}

	private IOutboundMessageNotification m_outboundMessageNotification;
	private OnmsInboundMessageNotification m_inboundMessageNotification;
	@SuppressWarnings("unused")
	private IGatewayStatusNotification m_gatewayStatusNotification;

	/**
	 * <p>Constructor for SmsServiceListFactoryBean.</p>
	 */
	public SmsServiceListFactoryBean() {
		
	}

	/**
	 * <p>setGatewayGroupList</p>
	 *
	 * @param groupList an array of {@link org.opennms.sms.reflector.smsservice.GatewayGroup} objects.
	 */
	public void setGatewayGroupList(GatewayGroup[] groupList) {
		m_gatewayGroups = groupList;
	}

	/**
	 * <p>afterPropertiesSet</p>
	 */
	@Override
	public void afterPropertiesSet() {
		m_services = new SmsService[m_gatewayGroups.length];

		int count = 0;
		for (GatewayGroup group : m_gatewayGroups) {
			AGateway[] gateways = group.getGateways();
			
			if (gateways.length == 0) {
				LOG.warn("A Gateway group was registered with ZERO gateways!");
			    return;
			}
			
			SmsServiceImpl smsService = new SmsServiceImpl();
			smsService.setOutboundNotification(m_outboundMessageNotification);
			smsService.setInboundNotification(m_inboundMessageNotification);
	        // smsService.setGatewayStatusNotification(m_gatewayStatusNotification);

			for(int i = 0; i < gateways.length; i++){
				
				try {
					if(smsService.getServiceStatus() == ServiceStatus.STARTED){
						smsService.stop();
					}
					smsService.addGateway(gateways[i]);
					
				} catch (final Exception e) {
				    LOG.warn("Unable to add gateway ({}) to SMS service", gateways[i], e);
				}
			}
			
			smsService.start();

			m_services[count++] = smsService;
		}
	}
	
	/**
	 * <p>getObject</p>
	 *
	 * @return an array of {@link org.opennms.sms.reflector.smsservice.SmsService} objects.
	 * @throws java.lang.Exception if any.
	 */
        @Override
	public SmsService[] getObject() throws Exception {
		return m_services;
	}

	/**
	 * <p>getObjectType</p>
	 *
	 * @return a {@link java.lang.Class} object.
	 */
        @Override
	public Class<? extends SmsService[]> getObjectType() {
		return SmsService[].class;
	}

	/**
	 * <p>isSingleton</p>
	 *
	 * @return a boolean.
	 */
        @Override
	public boolean isSingleton() {
		return true;
	}

}
