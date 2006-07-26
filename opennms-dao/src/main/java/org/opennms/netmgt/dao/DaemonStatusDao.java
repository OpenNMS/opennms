package org.opennms.netmgt.dao;
import java.util.Map;

import org.opennms.netmgt.model.ServiceDaemon;

public interface DaemonStatusDao {
	Map<String, ServiceInfo> getCurrentDaemonStatus();

	ServiceDaemon getServiceHandle(String service);
}
