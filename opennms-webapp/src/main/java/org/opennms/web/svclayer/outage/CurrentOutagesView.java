package org.opennms.web.svclayer.outage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;

public class CurrentOutagesView implements CurrentOutages {

	OutageService m_outageService;

	Collection<OnmsOutage> foundOutages;

	/* (non-Javadoc)
	 * @see org.opennms.web.svclayer.outage.CurrentOutages#setOutageService(org.opennms.web.svclayer.outage.OutageService)
	 */
	public void setOutageService(OutageService service) {
		m_outageService = service;
	}

	/* (non-Javadoc)
	 * @see org.opennms.web.svclayer.outage.CurrentOutages#currentOutages()
	 */
	public List currentOutages() {

		foundOutages = m_outageService.getCurrentOutages();

		HashMap ips = new HashMap<Integer, String>();
		HashMap nodes = new HashMap<Integer, String>();
		HashMap nodeids = new HashMap<Integer, String>();
		HashMap services = new HashMap<Integer, String>();

		List theTable = new ArrayList();

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		// SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

		for (Iterator iter = ((Collection<OnmsOutage>) foundOutages).iterator(); iter
				.hasNext();) {
			OnmsOutage outage = (OnmsOutage) iter.next();

			// The other bits and pieces, 3 loops later....
			// this just seems weird.

			OnmsMonitoredService monitoredService = outage
					.getMonitoredService();
			OnmsServiceType serviceType = monitoredService.getServiceType();
			OnmsIpInterface ipInterface = monitoredService.getIpInterface();

			ips.put(outage.getId(), ipInterface.getIpAddress());
			nodes.put(outage.getId(), ipInterface.getNode().getLabel());
			nodeids.put(outage.getId(), monitoredService.getNodeId());
			services.put(outage.getId(), serviceType.getName());

			Map outagerow = new HashMap();
			outagerow.put("id", (String) outage.getId().toString());
			outagerow.put("node", ipInterface.getNode().getLabel());
			outagerow.put("nodeid", (String) monitoredService.getNodeId()
					.toString());
			outagerow.put("interface", ipInterface.getIpAddress());
			outagerow.put("interfaceid", ipInterface.getId());
			outagerow.put("service", serviceType.getName());
			outagerow.put("serviceid", serviceType.getId());
			// if (outage.getIfLostService() != null) {
			// outagerow.put("down",
			// formatter.format(outage.getIfLostService()));
			// }
			if (outage.getIfLostService() != null) {
				outagerow.put("down", (Date) outage.getIfLostService());
			}
			if (outage.getIfRegainedService() != null) {
				outagerow.put("up", formatter.format(outage
						.getIfRegainedService()));
			}
			if (outage.getSuppressTime() != null) {
				outagerow.put("suppresstime", outage.getSuppressTime());
			}
			outagerow.put("suppressedby", outage.getSuppressedBy());
			theTable.add(outagerow);

		}

		return (List) theTable;

	}

}
