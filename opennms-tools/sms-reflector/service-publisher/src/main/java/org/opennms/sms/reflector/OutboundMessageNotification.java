package org.opennms.sms.reflector;

import java.util.List;

import org.smslib.IOutboundMessageNotification;
import org.smslib.OutboundMessage;

public class OutboundMessageNotification implements IOutboundMessageNotification {
	
	private List<IOutboundMessageNotification> m_listenerList;
	
	public OutboundMessageNotification(){
		super();
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
