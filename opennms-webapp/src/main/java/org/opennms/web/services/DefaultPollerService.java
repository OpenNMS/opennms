package org.opennms.web.services;

import java.util.Date;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;

public class DefaultPollerService implements PollerService {
	
	private EventProxy m_eventProxy;
	
	public void setEventProxy(EventProxy eventProxy) {
		m_eventProxy = eventProxy;
	}
	
	public void poll(OnmsMonitoredService monSvc, int pollResultId) {
		
		
		Event demandPollEvent = new Event();
		demandPollEvent.setUei(EventConstants.DEMAND_POLL_SERVICE_EVENT_UEI);
		demandPollEvent.setNodeid(monSvc.getNodeId());
		demandPollEvent.setInterface(monSvc.getIpAddress());
		demandPollEvent.setIfIndex(Integer.toString(monSvc.getIfIndex()));
		demandPollEvent.setService(monSvc.getServiceType().getName());
        demandPollEvent.setCreationTime(EventConstants.formatToString(new Date()));
        demandPollEvent.setTime(demandPollEvent.getCreationTime());
        demandPollEvent.setSource("PollerService");
		
		EventUtils.addParam(demandPollEvent, EventConstants.PARM_DEMAND_POLL_ID, pollResultId);

		sendEvent(demandPollEvent);
	}

	private void sendEvent(Event demandPollEvent) {
		try {
			m_eventProxy.send(demandPollEvent);
		} catch (EventProxyException e) {
			throw new ServiceException("Exception occurred sending demandPollEvent", e);
		}
	}

}
