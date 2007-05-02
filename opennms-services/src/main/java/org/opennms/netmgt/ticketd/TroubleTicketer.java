package org.opennms.netmgt.ticketd;

import java.util.Arrays;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;

public class TroubleTicketer implements SpringServiceDaemon, EventListener {
	
	private EventIpcManager m_eventIpcManager;
	
	public void setEventIpcManager(EventIpcManager eventIpcManager) {
		m_eventIpcManager = eventIpcManager;
	}
	
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_eventIpcManager != null, "property eventIpcManager must be set to a non-null value");

        String[] ueis = {
    			EventConstants.TROUBLETICKET_CANCEL_UEI,
    			EventConstants.TROUBLETICKET_CLOSE_UEI,
    			EventConstants.TROUBLETICKET_CREATE_UEI,
    			EventConstants.TROUBLETICKET_UPDATE_UEI
    	};
    	m_eventIpcManager.addEventListener(this, Arrays.asList(ueis));
    }

    public void start() throws Exception {
        // TODO Auto-generated method stub

    }

	public String getName() {
		return "TroubleTicketer";
	}

	public void onEvent(Event e) {
		if (EventConstants.TROUBLETICKET_CANCEL_UEI.equals(e.getUei())) {
			handleCancelTicket(e);
		} else if (EventConstants.TROUBLETICKET_CLOSE_UEI.equals(e.getUei())) {
			handleCloseTicket(e);
		} else if (EventConstants.TROUBLETICKET_CREATE_UEI.equals(e.getUei())) {
			handleCreateTicket(e);
		} else if (EventConstants.TROUBLETICKET_UPDATE_UEI.equals(e.getUei())) {
			handleUpdateTicket(e);
		}
	}

	private void handleCloseTicket(Event e) {
	}

	private void handleUpdateTicket(Event e) {
	}

	private void handleCreateTicket(Event e) {
	}

	private void handleCancelTicket(Event e) {
	}

}
