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

/**
 * <p>OutboundMessageNotification class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OutboundMessageNotification implements IOutboundMessageNotification, ApplicationContextAware {
    
    private static Logger log = LoggerFactory.getLogger(OutboundMessageNotification.class);
	
	private Collection<IOutboundMessageNotification> m_listenerList;
	private ApplicationContext m_applicationContext;
	
	/**
	 * <p>Constructor for OutboundMessageNotification.</p>
	 */
	public OutboundMessageNotification() {
	}
	
	/**
	 * <p>Constructor for OutboundMessageNotification.</p>
	 *
	 * @param listeners a {@link java.util.List} object.
	 */
	public OutboundMessageNotification(List<IOutboundMessageNotification> listeners){
	    m_listenerList = listeners;
	}
	
	/** {@inheritDoc} */
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
	
	/**
	 * <p>setListenerList</p>
	 *
	 * @param list a {@link java.util.List} object.
	 */
	public void setListenerList(List<IOutboundMessageNotification> list){
		m_listenerList = list;
	}

	/** {@inheritDoc} */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		m_applicationContext = applicationContext;
	}

}
