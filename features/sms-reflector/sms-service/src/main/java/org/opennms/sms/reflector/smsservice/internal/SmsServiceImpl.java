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
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.ICallNotification;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.IQueueSendingNotification;
import org.smslib.IUSSDNotification;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.Phonebook;
import org.smslib.SMSLibException;
import org.smslib.Service;
import org.smslib.Settings;
import org.smslib.TimeoutException;
import org.smslib.USSDRequest;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Message.MessageTypes;
import org.smslib.Service.ServiceStatus;
import org.smslib.balancing.LoadBalancer;
import org.smslib.crypto.KeyManager;
import org.smslib.queues.QueueManager;
import org.smslib.routing.Router;


public class SmsServiceImpl implements SmsService {
    
	private static Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);
	
	private Service m_service = new Service();
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
		public void process(String gatewayId, MessageTypes msgType, InboundMessage msg) {
			m_inboundNotification.process(SmsServiceImpl.this.findGateway(gatewayId), msgType, msg);
		}
		
		public OnmsInboundMessageNotification getOnmsInboundMessageNotification() {
			return m_inboundNotification;
		}
    }
	
	/**
     * @return the outboundListeners
     */
    public List<IOutboundMessageNotification> getOutboundListeners() {
        return m_outboundListeners;
    }

    /**
     * @param outboundListeners the outboundListeners to set
     */
    public void setOutboundListeners(List<IOutboundMessageNotification> outboundListeners) {
        m_outboundListeners = outboundListeners;
    }

    /**
     * @return the inboundListeners
     */
    public List<OnmsInboundMessageNotification> getInboundListeners() {
        return m_inboundListeners;
    }

    /**
     * @return the gatewayStatusListeners
     */
    public List<IGatewayStatusNotification> getGatewayStatusListeners() {
        return m_gatewayStatusListeners;
    }

    /**
     * @param gatewayStatusListeners the gatewayStatusListeners to set
     */
    public void setGatewayStatusListeners(List<IGatewayStatusNotification> gatewayStatusListeners) {
        m_gatewayStatusListeners = gatewayStatusListeners;
    }

    /**
     * @return the modemId
     */
    public String getModemId() {
        return m_modemId;
    }

    /**
     * @param modemId the modemId to set
     */
    public void setModemId(String modemId) {
        m_modemId = modemId;
    }

    /**
     * @return the modemPort
     */
    public String getModemPort() {
        return m_modemPort;
    }

    /**
     * @param modemPort the modemPort to set
     */
    public void setModemPort(String modemPort) {
        m_modemPort = modemPort;
    }

    /**
     * @return the baudRate
     */
    public int getBaudRate() {
        return m_baudRate;
    }

    /**
     * @param baudRate the baudRate to set
     */
    public void setBaudRate(int baudRate) {
        m_baudRate = baudRate;
    }

    /**
     * @return the manufacturer
     */
    public String getManufacturer() {
        return m_manufacturer;
    }

    /**
     * @param manufacturer the manufacturer to set
     */
    public void setManufacturer(String manufacturer) {
        this.m_manufacturer = manufacturer;
    }

    /**
     * @return the model
     */
    public String getModel() {
        return m_model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(String model) {
        this.m_model = model;
    }
    
    public void start() {
        try {
            m_service.startService();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    
    public void stop() {
    	m_service.stopService();
    }

    @SuppressWarnings("unchecked")
	public void refresh(Map properties) {
    	log.debug("Received a configuration refresh! " + properties);
	}
	
	public void addGateway(AGateway gateway) throws GatewayException {
		m_service.addGateway(gateway);
	}

	public boolean addToGroup(String groupName, String number) {
		return m_service.addToGroup(groupName, number);
	}

	public boolean createGroup(String groupName) {
		return m_service.createGroup(groupName);
	}

	public boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.deleteMessage(msg);
	}

	public ArrayList<String> expandGroup(String groupName) {
		return m_service.expandGroup(groupName);
	}

	public AGateway findGateway(String gatewayId) {
		return m_service.findGateway(gatewayId);
	}

	public ICallNotification getCallNotification() {
		return m_service.getCallNotification();
	}

	public IGatewayStatusNotification getGatewayStatusNotification() {
		return m_service.getGatewayStatusNotification();
	}

	public Collection<AGateway> getGateways() {
		return m_service.getGateways();
	}

	public AGateway[] getGatewaysNET() {
		return m_service.getGatewaysNET();
	}

	public int getInboundMessageCount(String gatewayId) {
		return m_service.getInboundMessageCount(gatewayId);
	}

	public int getInboundMessageCount(AGateway gateway) {
		return m_service.getInboundMessageCount(gateway);
	}

	public int getInboundMessageCount() {
		return m_service.getInboundMessageCount();
	}

	public OnmsInboundMessageNotification getInboundNotification() {
		return ((InboundNotificationAdapter)m_service.getInboundNotification()).getOnmsInboundMessageNotification();
	}

	public KeyManager getKeyManager() {
		return m_service.getKeyManager();
	}

	public LoadBalancer getLoadBalancer() {
		return m_service.getLoadBalancer();
	}

	public int getOutboundMessageCount(String gatewayId) {
		return m_service.getOutboundMessageCount(gatewayId);
	}

	public int getOutboundMessageCount(AGateway gateway) {
		return m_service.getOutboundMessageCount(gateway);
	}

	public int getOutboundMessageCount() {
		return m_service.getOutboundMessageCount();
	}

	public IOutboundMessageNotification getOutboundNotification() {
		return m_service.getOutboundNotification();
	}

	public QueueManager getQueueManager() {
		return m_service.getQueueManager();
	}

	public IQueueSendingNotification getQueueSendingNotification() {
		return m_service.getQueueSendingNotification();
	}

	public Router getRouter() {
		return m_service.getRouter();
	}

	public ServiceStatus getServiceStatus() {
		return m_service.getServiceStatus();
	}

	public Settings getSettings() {
		return m_service.getSettings();
	}

	public long getStartMillis() {
		return m_service.getStartMillis();
	}

	public boolean queueMessage(OutboundMessage msg) {
		return m_service.queueMessage(msg);
	}

	public boolean queueMessage(OutboundMessage msg, String gatewayId) {
		return m_service.queueMessage(msg, gatewayId);
	}

	public int queueMessages(Collection<OutboundMessage> msgList) {
		return m_service.queueMessages(msgList);
	}

	public int queueMessages(OutboundMessage[] msgArray) {
		return m_service.queueMessages(msgArray);
	}

	public int queueMessages(Collection<OutboundMessage> msgList, String gatewayId) {
		return m_service.queueMessages(msgList, gatewayId);
	}

	public int queueMessages(OutboundMessage[] msgArray, String gatewayId) {
		return m_service.queueMessages(msgArray, gatewayId);
	}

	public InboundMessage readMessage(String gatewayId, String memLoc, int memIndex) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessage(gatewayId, memLoc, memIndex);
	}

	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgList, msgClass);
	}

	public InboundMessage[] readMessages(MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass);
	}

	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgList, msgClass, gatewayId);
	}

	public InboundMessage[] readMessages(MessageClasses msgClass, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass, gatewayId);
	}

	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgList, msgClass, gateway);
	}

	public InboundMessage[] readMessages(MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass, gateway);
	}

	public int readPhonebook(Phonebook phonebook, String gatewayId) throws TimeoutException, GatewayException, IOException,InterruptedException {
		return m_service.readPhonebook(phonebook, gatewayId);
	}

	public boolean removeFromGroup(String groupName, String number) {
		return m_service.removeFromGroup(groupName, number);
	}

	public boolean removeGateway(AGateway gateway) throws GatewayException {
		return m_service.removeGateway(gateway);
	}

	public boolean removeGroup(String groupName) {
		return m_service.removeGroup(groupName);
	}

	public boolean removeMessage(OutboundMessage msg) {
		return m_service.removeMessage(msg);
	}

	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessage(msg);
	}

	public boolean sendMessage(OutboundMessage msg, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessage(msg, gatewayId);
	}

	public int sendMessages(Collection<OutboundMessage> msgList) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgList);
	}

	public int sendMessages(OutboundMessage[] msgArray) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgArray);
	}

	public int sendMessages(Collection<OutboundMessage> msgList, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgList, gatewayId);
	}

	public int sendMessages(OutboundMessage[] msgArray, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgArray, gatewayId);
	}

	public void setCallNotification(ICallNotification callNotification) {
		m_service.setCallNotification(callNotification);
	}

	public void setGatewayStatusNotification(IGatewayStatusNotification gatewayStatusNotification) {
		m_service.setGatewayStatusNotification(gatewayStatusNotification);
	}

	public void setInboundNotification(OnmsInboundMessageNotification inboundNotification) {
		InboundNotificationAdapter adapter = new InboundNotificationAdapter(inboundNotification);
		m_service.setInboundNotification(adapter);
	}

	public void setLoadBalancer(LoadBalancer loadBalancer) {
		m_service.setLoadBalancer(loadBalancer);
	}

	public void setOutboundNotification(IOutboundMessageNotification outboundNotification) {
		m_service.setOutboundNotification(outboundNotification);
	}

	public void setQueueSendingNotification(IQueueSendingNotification queueSendingNotification) {
		m_service.setQueueSendingNotification(queueSendingNotification);
	}

	public void setRouter(Router router) {
		m_service.setRouter(router);
	}

	public void startService() throws SMSLibException, TimeoutException, GatewayException, IOException, InterruptedException {
		m_service.startService();
	}

	public void stopService() throws TimeoutException, GatewayException, IOException, InterruptedException {
		m_service.stopService();
	}

	public void setService(Service m_service) {
		this.m_service = m_service;
	}

	public Service getService() {
		return m_service;
	}

	public void setGatewayGroup(List<GatewayGroup> m_gatewayGroup) {
		this.m_gatewayGroup = m_gatewayGroup;
	}

	public List<GatewayGroup> getGatewayGroup() {
		return m_gatewayGroup;
	}

    public void register(SmsServiceRegistrar smsServiceRegistrar) {
    	smsServiceRegistrar.registerSmsService(this);
    }
    
    public void unregister(SmsServiceRegistrar smsServiceRegistrar) {
    	smsServiceRegistrar.unregisterSmsService(this);
    }

    public IUSSDNotification getUSSDNotification() {
        return m_service.getUSSDNotification();
    }

    public boolean sendUSSDRequest(USSDRequest req, String gatewayId) throws GatewayException, TimeoutException, IOException, InterruptedException {
        return m_service.sendUSSDRequest(req, gatewayId);
    }

    public void setUSSDNotification(IUSSDNotification notif) {
        m_service.setUSSDNotification(notif);
    }

}
