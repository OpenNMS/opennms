package org.opennms.netmgt.model.updates;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;

public class MonitoredServiceUpdate {

	private final String m_serviceName;
	private final IpInterfaceUpdate m_ipInterfaceUpdate;
	private Map<String,CategoryUpdate> m_categoryUpdates = new HashMap<String,CategoryUpdate>();

	public MonitoredServiceUpdate(final IpInterfaceUpdate ipInterfaceUpdate, final String serviceName) {
		m_ipInterfaceUpdate = ipInterfaceUpdate;
		m_serviceName = serviceName;
	}

	public String getServiceName() {
		return m_serviceName;
	}

	public CategoryUpdate category(final String categoryName) {
		CategoryUpdate update = m_categoryUpdates.get(categoryName);
		if (update == null) {
			update = new CategoryUpdate(this, categoryName);
			m_categoryUpdates.put(categoryName, update);
		}
		return update;
	}

	public void apply(final OnmsIpInterface iface) {
		OnmsMonitoredService service = iface.getMonitoredServiceByServiceType(getServiceName());
		if (service == null) {
			service = new OnmsMonitoredService(iface, new OnmsServiceType(getServiceName()));
		}
		iface.addMonitoredService(service);
	}
}
