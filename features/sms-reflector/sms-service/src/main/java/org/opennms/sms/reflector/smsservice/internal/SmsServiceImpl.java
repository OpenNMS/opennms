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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opennms.sms.reflector.smsservice.GatewayGroup;
import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.opennms.sms.reflector.smsservice.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.*;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Message.MessageTypes;
import org.smslib.Service.ServiceStatus;
import org.smslib.balancing.LoadBalancer;
import org.smslib.crypto.KeyManager;
import org.smslib.queues.AbstractQueueManager;
import org.smslib.routing.Router;


/**
 * <p>SmsServiceImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SmsServiceImpl implements SmsService {
    
	private static Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);
	
	private Service m_service = Service.getInstance();
	private String m_modemId;
	private String m_modemPort;
	private int m_baudRate;
	private String m_manufacturer;
	private String m_model;
    private List<IOutboundMessageNotification> m_outboundListeners;
    private List<OnmsInboundMessageNotification> m_inboundListeners;
    private List<IGatewayStatusNotification> m_gatewayStatusListeners;
    private List<GatewayGroup> m_gatewayGroup;

    private class InboundNotificationAdapter implements IInboundMessageNotification {
    	private OnmsInboundMessageNotification m_inboundNotification;

    	public InboundNotificationAdapter(OnmsInboundMessageNotification onmsInbound) {
    		m_inboundNotification = onmsInbound;
    	}

        @Override
        public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg) {
            m_inboundNotification.process(SmsServiceImpl.this.findGateway(gateway.getGatewayId()), msgType, msg);
        }

		public OnmsInboundMessageNotification getOnmsInboundMessageNotification() {
			return m_inboundNotification;
		}

    }
	
    /**
     * <p>getOutboundListeners</p>
     *
     * @return the outboundListeners
     */
    public List<IOutboundMessageNotification> getOutboundListeners() {
        return m_outboundListeners;
    }

    /**
     * <p>setOutboundListeners</p>
     *
     * @param outboundListeners the outboundListeners to set
     */
    public void setOutboundListeners(List<IOutboundMessageNotification> outboundListeners) {
        m_outboundListeners = outboundListeners;
    }

    /**
     * <p>getInboundListeners</p>
     *
     * @return the inboundListeners
     */
    public List<OnmsInboundMessageNotification> getInboundListeners() {
        return m_inboundListeners;
    }

    /**
     * <p>getGatewayStatusListeners</p>
     *
     * @return the gatewayStatusListeners
     */
    public List<IGatewayStatusNotification> getGatewayStatusListeners() {
        return m_gatewayStatusListeners;
    }

    /**
     * <p>setGatewayStatusListeners</p>
     *
     * @param gatewayStatusListeners the gatewayStatusListeners to set
     */
    public void setGatewayStatusListeners(List<IGatewayStatusNotification> gatewayStatusListeners) {
        m_gatewayStatusListeners = gatewayStatusListeners;
    }

    /**
     * <p>getModemId</p>
     *
     * @return the modemId
     */
    public String getModemId() {
        return m_modemId;
    }

    /**
     * <p>setModemId</p>
     *
     * @param modemId the modemId to set
     */
    public void setModemId(String modemId) {
        m_modemId = modemId;
    }

    /**
     * <p>getModemPort</p>
     *
     * @return the modemPort
     */
    public String getModemPort() {
        return m_modemPort;
    }

    /**
     * <p>setModemPort</p>
     *
     * @param modemPort the modemPort to set
     */
    public void setModemPort(String modemPort) {
        m_modemPort = modemPort;
    }

    /**
     * <p>getBaudRate</p>
     *
     * @return the baudRate
     */
    public int getBaudRate() {
        return m_baudRate;
    }

    /**
     * <p>setBaudRate</p>
     *
     * @param baudRate the baudRate to set
     */
    public void setBaudRate(int baudRate) {
        m_baudRate = baudRate;
    }

    /**
     * <p>getManufacturer</p>
     *
     * @return the manufacturer
     */
    public String getManufacturer() {
        return m_manufacturer;
    }

    /**
     * <p>setManufacturer</p>
     *
     * @param manufacturer the manufacturer to set
     */
    public void setManufacturer(String manufacturer) {
        this.m_manufacturer = manufacturer;
    }

    /**
     * <p>getModel</p>
     *
     * @return the model
     */
    public String getModel() {
        return m_model;
    }

    /**
     * <p>setModel</p>
     *
     * @param model the model to set
     */
    public void setModel(String model) {
        this.m_model = model;
    }
    
    /**
     * <p>start</p>
     */
    public void start() {
        try {
            m_service.startService();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        
    }
    
    
    /**
     * <p>stop</p>
     */
    public void stop() throws InterruptedException, IOException, SMSLibException {
    	m_service.stopService();
    }

    /**
     * <p>refresh</p>
     *
     * @param properties a {@link java.util.Map} object.
     */
	public void refresh(Map<?,?> properties) {
	log.debug("Received a configuration refresh! {}", properties);
	}
	
	/** {@inheritDoc} */
        @Override
	public void addGateway(AGateway gateway) throws GatewayException {
		m_service.addGateway(gateway);
	}

	/** {@inheritDoc} */
        @Override
	public boolean addToGroup(String groupName, String number) {
		return m_service.addToGroup(groupName, number);
	}

	/** {@inheritDoc} */
        @Override
	public boolean createGroup(String groupName) {
		return m_service.createGroup(groupName);
	}

	/** {@inheritDoc} */
        @Override
	public boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.deleteMessage(msg);
	}

	/** {@inheritDoc} */
        @Override
	public ArrayList<String> expandGroup(String groupName) {
		return m_service.expandGroup(groupName);
	}

	/** {@inheritDoc} */
        @Override
	public AGateway findGateway(String gatewayId) {
		return m_service.findGateway(gatewayId);
	}

	/**
	 * <p>getCallNotification</p>
	 *
	 * @return a {@link org.smslib.ICallNotification} object.
	 */
        @Override
	public ICallNotification getCallNotification() {
		return m_service.getCallNotification();
	}

	/**
	 * <p>getGatewayStatusNotification</p>
	 *
	 * @return a {@link org.smslib.IGatewayStatusNotification} object.
	 */
        @Override
	public IGatewayStatusNotification getGatewayStatusNotification() {
		return m_service.getGatewayStatusNotification();
	}

	/**
	 * <p>getGateways</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
        @Override
	public Collection<AGateway> getGateways() {
		return m_service.getGateways();
	}

	/**
	 * <p>getGatewaysNET</p>
	 *
	 * @return an array of {@link org.smslib.AGateway} objects.
	 */
        @Override
	public AGateway[] getGatewaysNET() {
		return m_service.getGatewaysNET();
	}

	/** {@inheritDoc} */
        @Override
	public int getInboundMessageCount(String gatewayId) {
		return m_service.getInboundMessageCount(gatewayId);
	}

	/**
	 * <p>getInboundMessageCount</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return a int.
	 */
        @Override
	public int getInboundMessageCount(AGateway gateway) {
		return m_service.getInboundMessageCount(gateway);
	}

	/**
	 * <p>getInboundMessageCount</p>
	 *
	 * @return a int.
	 */
        @Override
	public int getInboundMessageCount() {
		return m_service.getInboundMessageCount();
	}

	/**
	 * <p>getInboundNotification</p>
	 *
	 * @return a {@link org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification} object.
	 */
        @Override
	public OnmsInboundMessageNotification getInboundNotification() {
		return ((InboundNotificationAdapter)m_service.getInboundMessageNotification()).getOnmsInboundMessageNotification();
	}

	/**
	 * <p>getKeyManager</p>
	 *
	 * @return a {@link org.smslib.crypto.KeyManager} object.
	 */
        @Override
	public KeyManager getKeyManager() {
		return m_service.getKeyManager();
	}

	/**
	 * <p>getLoadBalancer</p>
	 *
	 * @return a {@link org.smslib.balancing.LoadBalancer} object.
	 */
        @Override
	public LoadBalancer getLoadBalancer() {
		return m_service.getLoadBalancer();
	}

	/** {@inheritDoc} */
        @Override
	public int getOutboundMessageCount(String gatewayId) {
		return m_service.getOutboundMessageCount(gatewayId);
	}

	/**
	 * <p>getOutboundMessageCount</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return a int.
	 */
        @Override
	public int getOutboundMessageCount(AGateway gateway) {
		return m_service.getOutboundMessageCount(gateway);
	}

	/**
	 * <p>getOutboundMessageCount</p>
	 *
	 * @return a int.
	 */
        @Override
	public int getOutboundMessageCount() {
		return m_service.getOutboundMessageCount();
	}

	/**
	 * <p>getOutboundNotification</p>
	 *
	 * @return a {@link org.smslib.IOutboundMessageNotification} object.
	 */
        @Override
	public IOutboundMessageNotification getOutboundNotification() {
		return m_service.getOutboundMessageNotification();
	}

	/**
	 * <p>getQueueManager</p>
	 *
	 * @return a {@link org.smslib.queues.AbstractQueueManager} object.
	 */
        @Override
	public AbstractQueueManager getQueueManager() {
		return m_service.getQueueManager();
	}

	/**
	 * <p>getQueueSendingNotification</p>
	 *
	 * @return a {@link org.smslib.IQueueSendingNotification} object.
	 */
        @Override
	public IQueueSendingNotification getQueueSendingNotification() {
		return m_service.getQueueSendingNotification();
	}

	/**
	 * <p>getRouter</p>
	 *
	 * @return a {@link org.smslib.routing.Router} object.
	 */
        @Override
	public Router getRouter() {
		return m_service.getRouter();
	}

	/**
	 * <p>getServiceStatus</p>
	 *
	 * @return a {@link org.smslib.Service.ServiceStatus} object.
	 */
        @Override
	public ServiceStatus getServiceStatus() {
		return m_service.getServiceStatus();
	}

	/**
	 * <p>getSettings</p>
	 *
	 * @return a {@link org.smslib.Settings} object.
	 */
        @Override
	public Settings getSettings() {
		return m_service.getSettings();
	}

	/**
	 * <p>getStartMillis</p>
	 *
	 * @return a long.
	 */
        @Override
	public long getStartMillis() {
		return m_service.getStartMillis();
	}

	/** {@inheritDoc} */
        @Override
	public boolean queueMessage(OutboundMessage msg) {
		return m_service.queueMessage(msg);
	}

	/** {@inheritDoc} */
        @Override
	public boolean queueMessage(OutboundMessage msg, String gatewayId) {
		return m_service.queueMessage(msg, gatewayId);
	}

	/** {@inheritDoc} */
        @Override
	public int queueMessages(Collection<OutboundMessage> msgList) {
		return m_service.queueMessages(msgList);
	}

	/**
	 * <p>queueMessages</p>
	 *
	 * @param msgArray an array of {@link org.smslib.OutboundMessage} objects.
	 * @return a int.
	 */
        @Override
	public int queueMessages(OutboundMessage[] msgArray) {
		return m_service.queueMessages(msgArray);
	}

	/** {@inheritDoc} */
        @Override
	public int queueMessages(Collection<OutboundMessage> msgList, String gatewayId) {
		return m_service.queueMessages(msgList, gatewayId);
	}

	/** {@inheritDoc} */
        @Override
	public int queueMessages(OutboundMessage[] msgArray, String gatewayId) {
		return m_service.queueMessages(msgArray, gatewayId);
	}

	/** {@inheritDoc} */
        @Override
	public InboundMessage readMessage(String gatewayId, String memLoc, int memIndex) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessage(gatewayId, memLoc, memIndex);
	}

	/** {@inheritDoc} */
        @Override
	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgList, msgClass);
	}

	/** {@inheritDoc} */
        @Override
	public InboundMessage[] readMessages(MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass);
	}

	/** {@inheritDoc} */
        @Override
	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgList, msgClass, gatewayId);
	}

	/**
	 * <p>readMessages</p>
	 *
	 * @param msgClass a {@link org.smslib.InboundMessage.MessageClasses} object.
	 * @param gatewayId a {@link java.lang.String} object.
	 * @return an array of {@link org.smslib.InboundMessage} objects.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
        @Override
	public InboundMessage[] readMessages(MessageClasses msgClass, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass, gatewayId);
	}

	/**
	 * <p>readMessages</p>
	 *
	 * @param msgList a {@link java.util.Collection} object.
	 * @param msgClass a {@link org.smslib.InboundMessage.MessageClasses} object.
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return a int.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
        @Override
	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgList, msgClass, gateway);
	}

	/**
	 * <p>readMessages</p>
	 *
	 * @param msgClass a {@link org.smslib.InboundMessage.MessageClasses} object.
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return an array of {@link org.smslib.InboundMessage} objects.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
        @Override
	public InboundMessage[] readMessages(MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass, gateway);
	}

	/** {@inheritDoc} */
        @Override
	public int readPhonebook(Phonebook phonebook, String gatewayId) throws TimeoutException, GatewayException, IOException,InterruptedException {
		return m_service.readPhonebook(phonebook, gatewayId);
	}

	/** {@inheritDoc} */
        @Override
	public boolean removeFromGroup(String groupName, String number) {
		return m_service.removeFromGroup(groupName, number);
	}

	/** {@inheritDoc} */
        @Override
	public boolean removeGateway(AGateway gateway) throws GatewayException {
		return m_service.removeGateway(gateway);
	}

	/** {@inheritDoc} */
        @Override
	public boolean removeGroup(String groupName) {
		return m_service.removeGroup(groupName);
	}

	/** {@inheritDoc} */
        @Override
	public boolean removeMessage(OutboundMessage msg) {
		return m_service.removeMessage(msg);
	}

	/** {@inheritDoc} */
        @Override
	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessage(msg);
	}

	/** {@inheritDoc} */
        @Override
	public boolean sendMessage(OutboundMessage msg, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessage(msg, gatewayId);
	}

	/** {@inheritDoc} */
        @Override
	public int sendMessages(Collection<OutboundMessage> msgList) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgList);
	}

	/**
	 * <p>sendMessages</p>
	 *
	 * @param msgArray an array of {@link org.smslib.OutboundMessage} objects.
	 * @return a int.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
        @Override
	public int sendMessages(OutboundMessage[] msgArray) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgArray);
	}

	/** {@inheritDoc} */
        @Override
	public int sendMessages(Collection<OutboundMessage> msgList, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgList, gatewayId);
	}

	/** {@inheritDoc} */
        @Override
	public int sendMessages(OutboundMessage[] msgArray, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgArray, gatewayId);
	}

	/** {@inheritDoc} */
        @Override
	public void setCallNotification(ICallNotification callNotification) {
		m_service.setCallNotification(callNotification);
	}

	/** {@inheritDoc} */
        @Override
	public void setGatewayStatusNotification(IGatewayStatusNotification gatewayStatusNotification) {
		m_service.setGatewayStatusNotification(gatewayStatusNotification);
	}

	/** {@inheritDoc} */
        @Override
	public void setInboundNotification(OnmsInboundMessageNotification inboundNotification) {
		InboundNotificationAdapter adapter = new InboundNotificationAdapter(inboundNotification);
		m_service.setInboundMessageNotification(adapter);
	}

	/** {@inheritDoc} */
        @Override
	public void setLoadBalancer(LoadBalancer loadBalancer) {
		m_service.setLoadBalancer(loadBalancer);
	}

	/** {@inheritDoc} */
        @Override
	public void setOutboundNotification(IOutboundMessageNotification outboundNotification) {
		m_service.setOutboundMessageNotification(outboundNotification);
	}

	/** {@inheritDoc} */
        @Override
	public void setQueueSendingNotification(IQueueSendingNotification queueSendingNotification) {
		m_service.setQueueSendingNotification(queueSendingNotification);
	}

	/** {@inheritDoc} */
        @Override
	public void setRouter(Router router) {
		m_service.setRouter(router);
	}

	/**
	 * <p>startService</p>
	 *
	 * @throws org.smslib.SMSLibException if any.
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
        @Override
	public void startService() throws SMSLibException, TimeoutException, GatewayException, IOException, InterruptedException {
		m_service.startService();
	}

	/**
	 * <p>stopService</p>
	 *
	 * @throws org.smslib.TimeoutException if any.
	 * @throws org.smslib.GatewayException if any.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.InterruptedException if any.
	 */
        @Override
	public void stopService() throws SMSLibException, IOException, InterruptedException {
		m_service.stopService();
	}

	/**
	 * <p>setService</p>
	 *
	 * @param m_service a {@link org.smslib.Service} object.
	 */
	public void setService(Service m_service) {
		this.m_service = m_service;
	}

	/**
	 * <p>getService</p>
	 *
	 * @return a {@link org.smslib.Service} object.
	 */
	public Service getService() {
		return m_service;
	}

	/**
	 * <p>setGatewayGroup</p>
	 *
	 * @param m_gatewayGroup a {@link java.util.List} object.
	 */
	public void setGatewayGroup(List<GatewayGroup> m_gatewayGroup) {
		this.m_gatewayGroup = m_gatewayGroup;
	}

	/**
	 * <p>getGatewayGroup</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<GatewayGroup> getGatewayGroup() {
		return m_gatewayGroup;
	}

    /**
     * <p>register</p>
     *
     * @param smsServiceRegistrar a {@link org.opennms.sms.reflector.smsservice.internal.SmsServiceRegistrar} object.
     */
    public void register(SmsServiceRegistrar smsServiceRegistrar) {
    	smsServiceRegistrar.registerSmsService(this);
    }
    
    /**
     * <p>unregister</p>
     *
     * @param smsServiceRegistrar a {@link org.opennms.sms.reflector.smsservice.internal.SmsServiceRegistrar} object.
     */
    public void unregister(SmsServiceRegistrar smsServiceRegistrar) {
    	smsServiceRegistrar.unregisterSmsService(this);
    }

    /**
     * <p>getUSSDNotification</p>
     *
     * @return a {@link org.smslib.IUSSDNotification} object.
     */
        @Override
    public IUSSDNotification getUSSDNotification() {
        return m_service.getUSSDNotification();
    }

    /** {@inheritDoc} */
        @Override
    public boolean sendUSSDRequest(USSDRequest req, String gatewayId) throws GatewayException, TimeoutException, IOException, InterruptedException {
        return m_service.sendUSSDRequest(req, gatewayId);
    }

    /** {@inheritDoc} */
        @Override
    public void setUSSDNotification(IUSSDNotification notif) {
        m_service.setUSSDNotification(notif);
    }

}
