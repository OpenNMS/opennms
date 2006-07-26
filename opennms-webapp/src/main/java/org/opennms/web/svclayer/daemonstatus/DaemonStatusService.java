package org.opennms.web.svclayer.daemonstatus;

import java.util.Map;

import org.opennms.netmgt.dao.ServiceInfo;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = false)
public interface DaemonStatusService {

	@Transactional(readOnly = true)
	Map<String, ServiceInfo> getCurrentDaemonStatus();

	Map<String, ServiceInfo> startDaemon(String service);

	Map<String, ServiceInfo> stopDaemon(String service);

	Map<String, ServiceInfo> restartDaemon(String service);

	Map<String, ServiceInfo> performOperationOnDaemons(String operation,
			String[] deamons);
}
