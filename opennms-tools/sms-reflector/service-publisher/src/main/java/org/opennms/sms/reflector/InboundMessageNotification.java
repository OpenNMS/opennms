package org.opennms.sms.reflector;

import java.util.List;

import org.smslib.IInboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageTypes;

public class InboundMessageNotification implements IInboundMessageNotification {
	
	private List<IInboundMessageNotification> m_listenerList;
	
	public void process(String gatewayId, MessageTypes msgType, InboundMessage msg) {

		for(IInboundMessageNotification listener : m_listenerList){
			listener.process(gatewayId, msgType, msg);
		}
	}
	
	public void setListenerList(List<IInboundMessageNotification> listeners){
		m_listenerList = listeners;
	}

}
