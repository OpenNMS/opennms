package org.opennms.netmgt.collectd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;

public class ForceRescanState {
    
    private CollectionAgent m_agent;
    private EventProxy m_eventProxy;
    
    private boolean m_forceRescanSent = false;

    public ForceRescanState(CollectionAgent agent, EventProxy eventProxy) {
        m_agent = agent;
        m_eventProxy = eventProxy;
    }
    
    public EventProxy getEventProxy() {
        return m_eventProxy;
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public Event createForceResanEvent() {
        // create the event to be sent
        Event newEvent = new Event();
        
        newEvent.setUei(EventConstants.FORCE_RESCAN_EVENT_UEI);
        
        newEvent.setSource("SNMPServiceMonitor");
        
        newEvent.setInterface(m_agent.getHostAddress());
        
        newEvent.setService(SnmpCollector.SERVICE_NAME);
        
        newEvent.setHost(determineLocalHostName());
        
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));
        
        newEvent.setNodeid(m_agent.getNodeId());
        return newEvent;
    }

    String determineLocalHostName() {
    	// Get local host name (used when generating threshold events)
    	try {
    		return InetAddress.getLocalHost().getHostName();
    	} catch (UnknownHostException e) {
    		log().warn("initialize: Unable to resolve local host name.", e);
    		return "unresolved.host";
    	}
    }

    public CollectionAgent getAgent() {
        return m_agent;
    }

    /**
     * This method is responsible for building a Capsd forceRescan event object
     * and sending it out over the EventProxy.
     * @param eventProxy
     *            proxy over which an event may be sent to eventd
     * @param ifAddress
     *            interface address to which this event pertains
     * @param nodeId TODO
     */
    void sendForceRescanEvent() {
        // Log4j category
    	if (log().isDebugEnabled()) {
    		log().debug("generateForceRescanEvent: interface = " + getAgent().getHostAddress());
    	}
    
    	// Send event via EventProxy
    	try {
            getEventProxy().send(createForceResanEvent());
    	} catch (EventProxyException e) {
    		log().error("generateForceRescanEvent: Unable to send "
    				+ "forceRescan event.", e);
    	}
    }
    
    void rescanIndicated() {
        if (!m_forceRescanSent) {
            sendForceRescanEvent();
            m_forceRescanSent = true;
        }
    }

}
