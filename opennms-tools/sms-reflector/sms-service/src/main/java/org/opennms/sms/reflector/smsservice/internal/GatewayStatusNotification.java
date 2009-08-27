package org.opennms.sms.reflector.smsservice.internal;

import java.util.Collection;
import java.util.List;

import org.smslib.IGatewayStatusNotification;
import org.smslib.AGateway.GatewayStatuses;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class GatewayStatusNotification implements IGatewayStatusNotification, ApplicationContextAware {
	
	private Collection<IGatewayStatusNotification> m_listenerList;
	private ApplicationContext m_applicationContext;
	
	public GatewayStatusNotification() {
	}
	
	public GatewayStatusNotification(List<IGatewayStatusNotification> listeners) {
	    m_listenerList = listeners;
	}
	
	public void process(String gtwId, GatewayStatuses oldStatus, GatewayStatuses newStatus) {
		for(IGatewayStatusNotification listener : getListeners()){
			if (listener != this) {
				listener.process(gtwId, oldStatus, newStatus);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<IGatewayStatusNotification> getListeners() {
		if ( m_listenerList == null ) {
			m_listenerList = m_applicationContext.getBeansOfType(IGatewayStatusNotification.class).values();
		}
		return m_listenerList;
	}
	
	public void setListenerList(List<IGatewayStatusNotification> list){
		m_listenerList = list;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		m_applicationContext = applicationContext;
	}

}
