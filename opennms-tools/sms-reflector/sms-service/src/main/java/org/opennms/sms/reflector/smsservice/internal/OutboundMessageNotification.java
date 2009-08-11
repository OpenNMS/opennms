package org.opennms.sms.reflector.smsservice.internal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.IOutboundMessageNotification;
import org.smslib.OutboundMessage;

public class OutboundMessageNotification implements IOutboundMessageNotification {
    
    private static Logger log = LoggerFactory.getLogger(OutboundMessageNotification.class);
	
	private List<IOutboundMessageNotification> m_listenerList;
	
	public OutboundMessageNotification(List<IOutboundMessageNotification> listeners){
	    m_listenerList = listeners;
	}
	
	public void process(String gatewayId, OutboundMessage msg) {
	    
	    log.debug( "Forwading message to registered listeners: " + m_listenerList + " : " + msg );
	    
		for( IOutboundMessageNotification listener : m_listenerList )
		{
			listener.process( gatewayId, msg );
		}

	}
	
	public void setListenerList(List<IOutboundMessageNotification> list){
		m_listenerList = list;
	}

}
