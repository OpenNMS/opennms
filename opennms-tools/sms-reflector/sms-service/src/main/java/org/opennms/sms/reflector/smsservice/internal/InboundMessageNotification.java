package org.opennms.sms.reflector.smsservice.internal;

import java.util.List;

import org.opennms.sms.reflector.smsservice.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.IInboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageTypes;

public class InboundMessageNotification implements IInboundMessageNotification {

    private static Logger log = LoggerFactory.getLogger(InboundMessageNotification.class);
    
	private List<IInboundMessageNotification> m_listenerList;
    private SmsService m_smsService;
	
	public InboundMessageNotification(SmsService smsService, List<IInboundMessageNotification> listeners) {
	    m_smsService = smsService;
	    m_listenerList = listeners;
	}
	
	public void process(String gatewayId, MessageTypes msgType, InboundMessage msg) {

	    try {
	        for(IInboundMessageNotification listener : m_listenerList){
	            notifyListener(gatewayId, msgType, msg, listener);
	        }
	    }
	    finally {
	        deleteMessage(msg);
	    }
	}

    private void notifyListener(String gatewayId, MessageTypes msgType, InboundMessage msg, IInboundMessageNotification listener) {
        try {
            listener.process(gatewayId, msgType, msg);
        } catch (Throwable e) {
            log.error("Unexpected exception processing InboundMessage "+ msg + " listener: " + listener, e);
        }
    }

    private void deleteMessage(InboundMessage msg) {
        try {
            m_smsService.deleteMessage(msg);
        } catch (Exception e) {
            log.error("Unable to delete message " + msg, e);
        }
    }
	
	public void setListenerList(List<IInboundMessageNotification> listeners){
		m_listenerList = listeners;
	}

}
