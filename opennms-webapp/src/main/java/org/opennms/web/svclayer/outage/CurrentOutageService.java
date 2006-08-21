package org.opennms.web.svclayer.outage;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;

public class CurrentOutageService {

	public List theTable(Collection<OnmsOutage> foundOutages) {

		List theTable = new LinkedList();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		for (Iterator iter = foundOutages.iterator(); iter.hasNext();) {
			OnmsOutage outage = (OnmsOutage) iter.next();
			OnmsMonitoredService monitoredService = outage
					.getMonitoredService();
			OnmsServiceType serviceType = monitoredService.getServiceType();
			OnmsIpInterface ipInterface = monitoredService.getIpInterface();
			
			// ips.put(outage.getId(), ipInterface.getIpAddress());
			// nodes.put(outage.getId(), ipInterface.getNode().getLabel());
			// nodeids.put(outage.getId(), monitoredService.getNodeId());
			// services.put(outage.getId(), serviceType.getName());
			
			

			Map outagerow = new HashMap();
			outagerow.put("outageid", (String) outage.getId().toString());
			outagerow.put("node", ipInterface.getNode().getLabel());
			outagerow.put("nodeid", (String) monitoredService.getNodeId()
					.toString());
			outagerow.put("ipaddr", ipInterface.getIpAddress());
			outagerow.put("interfaceid", ipInterface.getId());
			outagerow.put("service", serviceType.getName());
			outagerow.put("serviceid", serviceType.getId());
			outagerow.put("eventid", outage.getEventBySvcLostEvent().getId());

			
			
			// if (outage.getIfLostService() != null) {
			// outagerow.put("down",
			// formatter.format(outage.getIfLostService()));
			// }

			if (outage.getIfLostService() != null) {
				outagerow
						.put("iflostservice", (Date) outage.getIfLostService());
			}

			if (outage.getIfRegainedService() != null) {
				outagerow.put("ifregainedservice", (Date) outage
						.getIfRegainedService());
			}

			if (outage.getSuppressTime() != null) {
				outagerow.put("suppresstime", outage.getSuppressTime());
			}

			outagerow.put("suppressedby", outage.getSuppressedBy());
			
			// Build a droplist for this outage
			
			theTable.add(outagerow);
		}
		
		return theTable;

	}

}
