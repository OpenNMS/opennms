package org.opennms.sms.reflector.smsservice.internal;

import java.util.Collection;
import java.util.List;

import org.opennms.sms.reflector.smsservice.OnmsInboundMessageNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageTypes;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class InboundMessageNotification implements OnmsInboundMessageNotification {

    private static Logger log = LoggerFactory.getLogger(InboundMessageNotification.class);
    
	private Collection<OnmsInboundMessageNotification> m_listenerList;
    // private SmsService m_smsService;
	@SuppressWarnings("unused")
	private ApplicationContext m_applicationContext;

	public InboundMessageNotification() {
	}

	public InboundMessageNotification(List<OnmsInboundMessageNotification> listeners) {
	    // m_smsService = smsService;
	    m_listenerList = listeners;
	}
	
	public Collection<OnmsInboundMessageNotification> getListeners() {
		return m_listenerList;
	}

	public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg) {
	    
	    deleteMessage(gateway, msg);

	    for(OnmsInboundMessageNotification listener : getListeners()){
	    	if (listener != this) {
	    		notifyListener(gateway, msgType, msg, listener);
	    	}
	    }

	}

    private void notifyListener(AGateway gateway, MessageTypes msgType, InboundMessage msg, OnmsInboundMessageNotification listener) {
        try {
            listener.process(gateway, msgType, msg);
        } catch (Throwable e) {
            log.error("Unexpected exception processing InboundMessage "+ msg + " listener: " + listener, e);
        }
    }

    private void deleteMessage(AGateway gateway, InboundMessage msg) {
        try {
            gateway.deleteMessage(msg);
        } catch (Exception e) {
            log.error("Unable to delete message " + msg, e);
        }
    }
	
	public void setListenerList(List<OnmsInboundMessageNotification> listeners){
		m_listenerList = listeners;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		m_applicationContext = applicationContext;
	}

}
