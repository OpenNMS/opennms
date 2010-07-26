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


/**
 * <p>SmsServiceImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    
    /**
     * <p>stop</p>
     */
    public void stop() {
    	m_service.stopService();
    }

    /**
     * <p>refresh</p>
     *
     * @param properties a {@link java.util.Map} object.
     */
    @SuppressWarnings("unchecked")
	public void refresh(Map properties) {
    	log.debug("Received a configuration refresh! " + properties);
	}
	
	/** {@inheritDoc} */
	public void addGateway(AGateway gateway) throws GatewayException {
		m_service.addGateway(gateway);
	}

	/** {@inheritDoc} */
	public boolean addToGroup(String groupName, String number) {
		return m_service.addToGroup(groupName, number);
	}

	/** {@inheritDoc} */
	public boolean createGroup(String groupName) {
		return m_service.createGroup(groupName);
	}

	/** {@inheritDoc} */
	public boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.deleteMessage(msg);
	}

	/** {@inheritDoc} */
	public ArrayList<String> expandGroup(String groupName) {
		return m_service.expandGroup(groupName);
	}

	/** {@inheritDoc} */
	public AGateway findGateway(String gatewayId) {
		return m_service.findGateway(gatewayId);
	}

	/**
	 * <p>getCallNotification</p>
	 *
	 * @return a {@link org.smslib.ICallNotification} object.
	 */
	public ICallNotification getCallNotification() {
		return m_service.getCallNotification();
	}

	/**
	 * <p>getGatewayStatusNotification</p>
	 *
	 * @return a {@link org.smslib.IGatewayStatusNotification} object.
	 */
	public IGatewayStatusNotification getGatewayStatusNotification() {
		return m_service.getGatewayStatusNotification();
	}

	/**
	 * <p>getGateways</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<AGateway> getGateways() {
		return m_service.getGateways();
	}

	/**
	 * <p>getGatewaysNET</p>
	 *
	 * @return an array of {@link org.smslib.AGateway} objects.
	 */
	public AGateway[] getGatewaysNET() {
		return m_service.getGatewaysNET();
	}

	/** {@inheritDoc} */
	public int getInboundMessageCount(String gatewayId) {
		return m_service.getInboundMessageCount(gatewayId);
	}

	/**
	 * <p>getInboundMessageCount</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return a int.
	 */
	public int getInboundMessageCount(AGateway gateway) {
		return m_service.getInboundMessageCount(gateway);
	}

	/**
	 * <p>getInboundMessageCount</p>
	 *
	 * @return a int.
	 */
	public int getInboundMessageCount() {
		return m_service.getInboundMessageCount();
	}

	/**
	 * <p>getInboundNotification</p>
	 *
	 * @return a {@link org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification} object.
	 */
	public OnmsInboundMessageNotification getInboundNotification() {
		return ((InboundNotificationAdapter)m_service.getInboundNotification()).getOnmsInboundMessageNotification();
	}

	/**
	 * <p>getKeyManager</p>
	 *
	 * @return a {@link org.smslib.crypto.KeyManager} object.
	 */
	public KeyManager getKeyManager() {
		return m_service.getKeyManager();
	}

	/**
	 * <p>getLoadBalancer</p>
	 *
	 * @return a {@link org.smslib.balancing.LoadBalancer} object.
	 */
	public LoadBalancer getLoadBalancer() {
		return m_service.getLoadBalancer();
	}

	/** {@inheritDoc} */
	public int getOutboundMessageCount(String gatewayId) {
		return m_service.getOutboundMessageCount(gatewayId);
	}

	/**
	 * <p>getOutboundMessageCount</p>
	 *
	 * @param gateway a {@link org.smslib.AGateway} object.
	 * @return a int.
	 */
	public int getOutboundMessageCount(AGateway gateway) {
		return m_service.getOutboundMessageCount(gateway);
	}

	/**
	 * <p>getOutboundMessageCount</p>
	 *
	 * @return a int.
	 */
	public int getOutboundMessageCount() {
		return m_service.getOutboundMessageCount();
	}

	/**
	 * <p>getOutboundNotification</p>
	 *
	 * @return a {@link org.smslib.IOutboundMessageNotification} object.
	 */
	public IOutboundMessageNotification getOutboundNotification() {
		return m_service.getOutboundNotification();
	}

	/**
	 * <p>getQueueManager</p>
	 *
	 * @return a {@link org.smslib.queues.QueueManager} object.
	 */
	public QueueManager getQueueManager() {
		return m_service.getQueueManager();
	}

	/**
	 * <p>getQueueSendingNotification</p>
	 *
	 * @return a {@link org.smslib.IQueueSendingNotification} object.
	 */
	public IQueueSendingNotification getQueueSendingNotification() {
		return m_service.getQueueSendingNotification();
	}

	/**
	 * <p>getRouter</p>
	 *
	 * @return a {@link org.smslib.routing.Router} object.
	 */
	public Router getRouter() {
		return m_service.getRouter();
	}

	/**
	 * <p>getServiceStatus</p>
	 *
	 * @return a {@link org.smslib.Service.ServiceStatus} object.
	 */
	public ServiceStatus getServiceStatus() {
		return m_service.getServiceStatus();
	}

	/**
	 * <p>getSettings</p>
	 *
	 * @return a {@link org.smslib.Settings} object.
	 */
	public Settings getSettings() {
		return m_service.getSettings();
	}

	/**
	 * <p>getStartMillis</p>
	 *
	 * @return a long.
	 */
	public long getStartMillis() {
		return m_service.getStartMillis();
	}

	/** {@inheritDoc} */
	public boolean queueMessage(OutboundMessage msg) {
		return m_service.queueMessage(msg);
	}

	/** {@inheritDoc} */
	public boolean queueMessage(OutboundMessage msg, String gatewayId) {
		return m_service.queueMessage(msg, gatewayId);
	}

	/** {@inheritDoc} */
	public int queueMessages(Collection<OutboundMessage> msgList) {
		return m_service.queueMessages(msgList);
	}

	/**
	 * <p>queueMessages</p>
	 *
	 * @param msgArray an array of {@link org.smslib.OutboundMessage} objects.
	 * @return a int.
	 */
	public int queueMessages(OutboundMessage[] msgArray) {
		return m_service.queueMessages(msgArray);
	}

	/** {@inheritDoc} */
	public int queueMessages(Collection<OutboundMessage> msgList, String gatewayId) {
		return m_service.queueMessages(msgList, gatewayId);
	}

	/** {@inheritDoc} */
	public int queueMessages(OutboundMessage[] msgArray, String gatewayId) {
		return m_service.queueMessages(msgArray, gatewayId);
	}

	/** {@inheritDoc} */
	public InboundMessage readMessage(String gatewayId, String memLoc, int memIndex) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessage(gatewayId, memLoc, memIndex);
	}

	/** {@inheritDoc} */
	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgList, msgClass);
	}

	/** {@inheritDoc} */
	public InboundMessage[] readMessages(MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass);
	}

	/** {@inheritDoc} */
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
	public InboundMessage[] readMessages(MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.readMessages(msgClass, gateway);
	}

	/** {@inheritDoc} */
	public int readPhonebook(Phonebook phonebook, String gatewayId) throws TimeoutException, GatewayException, IOException,InterruptedException {
		return m_service.readPhonebook(phonebook, gatewayId);
	}

	/** {@inheritDoc} */
	public boolean removeFromGroup(String groupName, String number) {
		return m_service.removeFromGroup(groupName, number);
	}

	/** {@inheritDoc} */
	public boolean removeGateway(AGateway gateway) throws GatewayException {
		return m_service.removeGateway(gateway);
	}

	/** {@inheritDoc} */
	public boolean removeGroup(String groupName) {
		return m_service.removeGroup(groupName);
	}

	/** {@inheritDoc} */
	public boolean removeMessage(OutboundMessage msg) {
		return m_service.removeMessage(msg);
	}

	/** {@inheritDoc} */
	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessage(msg);
	}

	/** {@inheritDoc} */
	public boolean sendMessage(OutboundMessage msg, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessage(msg, gatewayId);
	}

	/** {@inheritDoc} */
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
	public int sendMessages(OutboundMessage[] msgArray) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgArray);
	}

	/** {@inheritDoc} */
	public int sendMessages(Collection<OutboundMessage> msgList, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgList, gatewayId);
	}

	/** {@inheritDoc} */
	public int sendMessages(OutboundMessage[] msgArray, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException {
		return m_service.sendMessages(msgArray, gatewayId);
	}

	/** {@inheritDoc} */
	public void setCallNotification(ICallNotification callNotification) {
		m_service.setCallNotification(callNotification);
	}

	/** {@inheritDoc} */
	public void setGatewayStatusNotification(IGatewayStatusNotification gatewayStatusNotification) {
		m_service.setGatewayStatusNotification(gatewayStatusNotification);
	}

	/** {@inheritDoc} */
	public void setInboundNotification(OnmsInboundMessageNotification inboundNotification) {
		InboundNotificationAdapter adapter = new InboundNotificationAdapter(inboundNotification);
		m_service.setInboundNotification(adapter);
	}

	/** {@inheritDoc} */
	public void setLoadBalancer(LoadBalancer loadBalancer) {
		m_service.setLoadBalancer(loadBalancer);
	}

	/** {@inheritDoc} */
	public void setOutboundNotification(IOutboundMessageNotification outboundNotification) {
		m_service.setOutboundNotification(outboundNotification);
	}

	/** {@inheritDoc} */
	public void setQueueSendingNotification(IQueueSendingNotification queueSendingNotification) {
		m_service.setQueueSendingNotification(queueSendingNotification);
	}

	/** {@inheritDoc} */
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
	public void stopService() throws TimeoutException, GatewayException, IOException, InterruptedException {
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
    public IUSSDNotification getUSSDNotification() {
        return m_service.getUSSDNotification();
    }

    /** {@inheritDoc} */
    public boolean sendUSSDRequest(USSDRequest req, String gatewayId) throws GatewayException, TimeoutException, IOException, InterruptedException {
        return m_service.sendUSSDRequest(req, gatewayId);
    }

    /** {@inheritDoc} */
    public void setUSSDNotification(IUSSDNotification notif) {
        m_service.setUSSDNotification(notif);
    }

}
