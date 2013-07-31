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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.*;
import org.smslib.Service.ServiceStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>GatewayGroupListener class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GatewayGroupListener implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(GatewayGroupListener.class);

    private SmsServiceRegistrar m_smsServiceRegistrar;
    private Map<GatewayGroup, SmsServiceImpl> m_services = new HashMap<GatewayGroup, SmsServiceImpl>();
    private List<IOutboundMessageNotification> m_outboundListeners;
    private List<OnmsInboundMessageNotification> m_inboundListeners;
    private List<IGatewayStatusNotification> m_gatewayStatusListeners;
    private List<IUSSDNotification> m_ussdListeners;

    /**
     * <p>onGatewayGroupRegistered</p>
     *
     * @param gatewayGroup a {@link org.opennms.sms.reflector.smsservice.GatewayGroup} object.
     * @param properties a {@link java.util.Map} object.
     */
    public void onGatewayGroupRegistered(GatewayGroup gatewayGroup, Map<String, Object> properties){
        AGateway[] gateways = gatewayGroup.getGateways();

        if (gateways.length == 0) {
            LOG.error("A Gateway group was registered with ZERO gateways!");
            return;
        }

        SmsServiceImpl smsService = new SmsServiceImpl();
        smsService.setOutboundNotification(new OutboundMessageNotification(getOutboundListeners()));
        smsService.setInboundNotification(new InboundMessageNotification(getInboundListeners()));
        smsService.setGatewayStatusNotification(new GatewayStatusNotification(getGatewayStatusListeners()));
        smsService.setUSSDNotification(new UssdNotificationDispatcher(getUssdListeners()));

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

        smsService.register(m_smsServiceRegistrar);

        m_services.put(gatewayGroup, smsService);


    }

    /**
     * <p>onGatewayGroupUnRegistered</p>
     *
     * @param gatewayGroup a {@link org.opennms.sms.reflector.smsservice.GatewayGroup} object.
     * @param properties a {@link java.util.Map} object.
     */
    public void onGatewayGroupUnRegistered(GatewayGroup gatewayGroup, Map<?,?> properties){

        SmsServiceImpl service = m_services.get(gatewayGroup);

        service.unregister(m_smsServiceRegistrar);

        try {
            service.stop();
        } catch (Exception e) {
            LOG.warn("Unable to stop SMS service: {}", gatewayGroup, e);
        }

    }

    @SuppressWarnings("unused")
    private boolean gatewayIdMatches(Collection<AGateway> gateways, AGateway[] aGateways) {
        for(AGateway serviceGateway : gateways){
            for(AGateway groupGateway : aGateways){
                if(serviceGateway.getGatewayId() == groupGateway.getGatewayId()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>setOutboundListeners</p>
     *
     * @param outboundListeners a {@link java.util.List} object.
     */
    public void setOutboundListeners(List<IOutboundMessageNotification> outboundListeners) {
        m_outboundListeners = outboundListeners;
    }

    /**
     * <p>getOutboundListeners</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<IOutboundMessageNotification> getOutboundListeners() {
        return m_outboundListeners;
    }

    /**
     * <p>setInboundListeners</p>
     *
     * @param inboundListeners a {@link java.util.List} object.
     */
    public void setInboundListeners(List<OnmsInboundMessageNotification> inboundListeners) {
        m_inboundListeners = inboundListeners;
    }

    /**
     * <p>getInboundListeners</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsInboundMessageNotification> getInboundListeners() {
        return m_inboundListeners;
    }

    /**
     * <p>setGatewayStatusListeners</p>
     *
     * @param gatewayStatusListeners a {@link java.util.List} object.
     */
    public void setGatewayStatusListeners(List<IGatewayStatusNotification> gatewayStatusListeners) {
        m_gatewayStatusListeners = gatewayStatusListeners;
    }

    /**
     * <p>getGatewayStatusListeners</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<IGatewayStatusNotification> getGatewayStatusListeners() {
        return m_gatewayStatusListeners;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_smsServiceRegistrar, "the smsServiceRegistrar must not be null");

    }

    /**
     * <p>setSmsServiceRegistrar</p>
     *
     * @param smsServiceRegistrar a {@link org.opennms.sms.reflector.smsservice.internal.SmsServiceRegistrar} object.
     */
    public void setSmsServiceRegistrar(SmsServiceRegistrar smsServiceRegistrar) {
        m_smsServiceRegistrar = smsServiceRegistrar;
    }

    /**
     * <p>getSmsServiceRegistrar</p>
     *
     * @return a {@link org.opennms.sms.reflector.smsservice.internal.SmsServiceRegistrar} object.
     */
    public SmsServiceRegistrar getSmsServiceRegistrar() {
        return m_smsServiceRegistrar;
    }
    
    /**
     * <p>setUssdListeners</p>
     *
     * @param listeners a {@link java.util.List} object.
     */
    public void setUssdListeners(List<IUSSDNotification> listeners) {
        m_ussdListeners = listeners;
    }
    
    /**
     * <p>getUssdListeners</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<IUSSDNotification> getUssdListeners() {
        return m_ussdListeners;
    }

}
