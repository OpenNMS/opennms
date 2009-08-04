package org.opennms.sms.reflector.smsservice.internal;

import java.util.List;

import org.smslib.IGatewayStatusNotification;
import org.smslib.AGateway.GatewayStatuses;

public class GatewayStatusNotification implements IGatewayStatusNotification {
	
	private List<IGatewayStatusNotification> m_listenerList;
	
	public GatewayStatusNotification(List<IGatewayStatusNotification> listeners) {
	    m_listenerList = listeners;
	}
	
	public void process(String gtwId, GatewayStatuses oldStatus, GatewayStatuses newStatus) {
		for(IGatewayStatusNotification listener : m_listenerList){
			listener.process(gtwId, oldStatus, newStatus);
		}
	}
	
	public void setListenerList(List<IGatewayStatusNotification> list){
		m_listenerList = list;
	}

}
