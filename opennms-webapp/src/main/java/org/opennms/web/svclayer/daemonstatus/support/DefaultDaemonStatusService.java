package org.opennms.web.svclayer.daemonstatus.support;

import java.util.Map;

import org.opennms.netmgt.model.ServiceDaemon;
import org.opennms.netmgt.dao.DaemonStatusDao;
import org.opennms.netmgt.dao.ServiceInfo;
import org.opennms.web.svclayer.daemonstatus.DaemonStatusService;

public class DefaultDaemonStatusService implements DaemonStatusService {
	
	private DaemonStatusDao daemonStatusDao; 

	public void setDaemonStatusDao(DaemonStatusDao daemonStatusDao) {
		this.daemonStatusDao = daemonStatusDao;
	}
	
	public Map<String, ServiceInfo> getCurrentDaemonStatus() {
		// TODO Auto-generated method stub
		Map<String, ServiceInfo> info = daemonStatusDao.getCurrentDaemonStatus();
        return info;
	}

	public Map<String, ServiceInfo> performOperationOnDaemons(String operation, String[] daemons) {
		// TODO Auto-generated method stub
		for(int i = 0; i < daemons.length; i++){
			if(operation.equalsIgnoreCase("start")) {
				startDaemon(daemons[i]);
			} else if(operation.equalsIgnoreCase("stop")) {
				stopDaemon(daemons[i]);				
			} else if(operation.equalsIgnoreCase("restart")) {
				restartDaemon(daemons[i]);
			} else if(operation.equalsIgnoreCase("refresh")) {
				// do nothing
			} else {
				// TBD raise an exception...or ignore...
			}
		}
		return getCurrentDaemonStatus();
	}

	public Map<String, ServiceInfo> restartDaemon(String service) {
		ServiceDaemon serviceDaemon = daemonStatusDao.getServiceHandle(service);
		serviceDaemon.stop();
		serviceDaemon.start();
		return getCurrentDaemonStatus();
	}

	public Map<String, ServiceInfo> startDaemon(String service) {
		ServiceDaemon serviceDaemon = daemonStatusDao.getServiceHandle(service);
		serviceDaemon.start();
		return getCurrentDaemonStatus();
	}

	public Map<String, ServiceInfo> stopDaemon(String service) {
		// TODO Auto-generated method stub
		return getCurrentDaemonStatus();
	}

}
