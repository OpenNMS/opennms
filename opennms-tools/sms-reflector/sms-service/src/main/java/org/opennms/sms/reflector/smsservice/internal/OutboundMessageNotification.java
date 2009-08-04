package org.opennms.sms.reflector.smsservice.internal;

import java.util.List;

import org.smslib.IOutboundMessageNotification;
import org.smslib.OutboundMessage;

public class OutboundMessageNotification implements IOutboundMessageNotification {
	
	private List<IOutboundMessageNotification> m_listenerList;
	
	public OutboundMessageNotification(List<IOutboundMessageNotification> listeners){
	    m_listenerList = listeners;
	}
	
	public void process(String gatewayId, OutboundMessage msg) {
		for(IOutboundMessageNotification listener : m_listenerList){
			listener.process(gatewayId, msg);
		}

	}
	
	public void setListenerList(List<IOutboundMessageNotification> list){
		m_listenerList = list;
	}

}
