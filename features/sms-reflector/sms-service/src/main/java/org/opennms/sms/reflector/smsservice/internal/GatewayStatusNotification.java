package org.opennms.sms.reflector.smsservice.internal;

import java.util.Collection;
import java.util.List;

import org.smslib.IGatewayStatusNotification;
import org.smslib.AGateway.GatewayStatuses;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>GatewayStatusNotification class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GatewayStatusNotification implements IGatewayStatusNotification, ApplicationContextAware {
	
	private Collection<IGatewayStatusNotification> m_listenerList;
	private ApplicationContext m_applicationContext;
	
	/**
	 * <p>Constructor for GatewayStatusNotification.</p>
	 */
	public GatewayStatusNotification() {
	}
	
	/**
	 * <p>Constructor for GatewayStatusNotification.</p>
	 *
	 * @param listeners a {@link java.util.List} object.
	 */
	public GatewayStatusNotification(List<IGatewayStatusNotification> listeners) {
	    m_listenerList = listeners;
	}
	
	/** {@inheritDoc} */
	public void process(String gtwId, GatewayStatuses oldStatus, GatewayStatuses newStatus) {
		for(IGatewayStatusNotification listener : getListeners()){
			if (listener != this) {
				listener.process(gtwId, oldStatus, newStatus);
			}
		}
	}

	private Collection<IGatewayStatusNotification> getListeners() {
		if ( m_listenerList == null ) {
			m_listenerList = m_applicationContext.getBeansOfType(IGatewayStatusNotification.class).values();
		}
		return m_listenerList;
	}
	
	/**
	 * <p>setListenerList</p>
	 *
	 * @param list a {@link java.util.List} object.
	 */
	public void setListenerList(List<IGatewayStatusNotification> list){
		m_listenerList = list;
	}

	/** {@inheritDoc} */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		m_applicationContext = applicationContext;
	}

}
