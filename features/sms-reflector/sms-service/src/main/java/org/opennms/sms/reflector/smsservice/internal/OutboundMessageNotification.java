package org.opennms.sms.reflector.smsservice.internal;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.IOutboundMessageNotification;
import org.smslib.OutboundMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class OutboundMessageNotification implements IOutboundMessageNotification, ApplicationContextAware {
    
    private static Logger log = LoggerFactory.getLogger(OutboundMessageNotification.class);
	
	private Collection<IOutboundMessageNotification> m_listenerList;
	private ApplicationContext m_applicationContext;
	
	public OutboundMessageNotification() {
	}
	
	public OutboundMessageNotification(List<IOutboundMessageNotification> listeners){
	    m_listenerList = listeners;
	}
	
	public void process(String gatewayId, OutboundMessage msg) {
	    
	    log.debug( "Forwarding message to registered listeners: " + getListeners() + " : " + msg );
	    
		for( IOutboundMessageNotification listener : getListeners() )
		{
			if (listener != this) {
				listener.process( gatewayId, msg );
			}
		}

	}

	private Collection<IOutboundMessageNotification> getListeners() {
		if (m_listenerList == null) {
			m_listenerList = m_applicationContext.getBeansOfType(IOutboundMessageNotification.class).values();
		}
		return m_listenerList;
	}
	
	public void setListenerList(List<IOutboundMessageNotification> list){
		m_listenerList = list;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		m_applicationContext = applicationContext;
	}

}
