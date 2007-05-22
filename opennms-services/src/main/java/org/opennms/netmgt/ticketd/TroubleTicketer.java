package org.opennms.netmgt.ticketd;

import java.util.Arrays;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.capsd.InsufficientInformationException;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;

public class TroubleTicketer implements SpringServiceDaemon, EventListener {
	
    private boolean m_initialized = false;
	private EventIpcManager m_eventIpcManager;
    private TicketerServiceLayer m_ticketerServiceLayer;
	
	public void setEventIpcManager(EventIpcManager eventIpcManager) {
		m_eventIpcManager = eventIpcManager;
	}
    
	
    public void setTicketerServiceLayer(TicketerServiceLayer ticketerServiceLayer) {
        m_ticketerServiceLayer = ticketerServiceLayer;
    }


    public void afterPropertiesSet() throws Exception {
        Assert.state(!m_initialized, "shouldn't be calling afterProperties set more than once");
        Assert.state(m_eventIpcManager != null, "property eventIpcManager must be set to a non-null value");
        Assert.state(m_ticketerServiceLayer != null, "property ticketerServiceLayer must be set to a non-null value");

        String[] ueis = {
    			EventConstants.TROUBLETICKET_CANCEL_UEI,
    			EventConstants.TROUBLETICKET_CLOSE_UEI,
    			EventConstants.TROUBLETICKET_CREATE_UEI,
    			EventConstants.TROUBLETICKET_UPDATE_UEI
    	};
    	m_eventIpcManager.addEventListener(this, Arrays.asList(ueis));
        
        m_initialized = true;
    }

    public void start() throws Exception {
        // DO NOTHING?
    }

	public String getName() {
		return "TroubleTicketer";
	}

	public void onEvent(Event e) {
        try {
		if (EventConstants.TROUBLETICKET_CANCEL_UEI.equals(e.getUei())) {
			handleCancelTicket(e);
		} else if (EventConstants.TROUBLETICKET_CLOSE_UEI.equals(e.getUei())) {
			handleCloseTicket(e);
		} else if (EventConstants.TROUBLETICKET_CREATE_UEI.equals(e.getUei())) {
			handleCreateTicket(e);
		} else if (EventConstants.TROUBLETICKET_UPDATE_UEI.equals(e.getUei())) {
			handleUpdateTicket(e);
		}
        } catch (InsufficientInformationException ex) {
            log().warn("Unable to create trouble ticket due to lack of information: "+ex.getMessage());
        }
	}

	private Category log() {
	    return ThreadCategory.getInstance(getClass());
    }


    private void handleCloseTicket(Event e) throws InsufficientInformationException {
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_ID);
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_UEI);
        EventUtils.requireParm(e, EventConstants.PARM_USER);
        EventUtils.requireParm(e, EventConstants.PARM_TROUBLE_TICKET);
        
        int alarmId = EventUtils.getIntParm(e, EventConstants.PARM_ALARM_ID);
        String ticketId = EventUtils.getParm(e, EventConstants.PARM_TROUBLE_TICKET);
        
        m_ticketerServiceLayer.closeTicketForAlarm(alarmId, ticketId);
	}

	private void handleUpdateTicket(Event e) throws InsufficientInformationException {
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_ID);
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_UEI);
        EventUtils.requireParm(e, EventConstants.PARM_USER);
        EventUtils.requireParm(e, EventConstants.PARM_TROUBLE_TICKET);

        int alarmId = EventUtils.getIntParm(e, EventConstants.PARM_ALARM_ID);
        String ticketId = EventUtils.getParm(e, EventConstants.PARM_TROUBLE_TICKET);
        
        m_ticketerServiceLayer.updateTicketForAlarm(alarmId, ticketId);
    }

	private void handleCreateTicket(Event e) throws InsufficientInformationException {
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_ID);
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_UEI);
        EventUtils.requireParm(e, EventConstants.PARM_USER);

        int alarmId = EventUtils.getIntParm(e, EventConstants.PARM_ALARM_ID);
        
        m_ticketerServiceLayer.createTicketForAlarm(alarmId);
	}

	private void handleCancelTicket(Event e) throws InsufficientInformationException {
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_ID);
        EventUtils.requireParm(e, EventConstants.PARM_ALARM_UEI);
        EventUtils.requireParm(e, EventConstants.PARM_USER);
        EventUtils.requireParm(e, EventConstants.PARM_TROUBLE_TICKET);

        int alarmId = EventUtils.getIntParm(e, EventConstants.PARM_ALARM_ID);
        String ticketId = EventUtils.getParm(e, EventConstants.PARM_TROUBLE_TICKET);
        
        m_ticketerServiceLayer.cancelTicketForAlarm(alarmId, ticketId);
	}

}
