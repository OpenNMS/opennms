package org.opennms.netmgt.dao;

import java.util.Collection;
import java.util.Set;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsPackage;

public class CollectorConfigDaoStub implements CollectorConfigDao {

	public OnmsPackage load(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public OnmsPackage get(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public OnmsPackage findPackageForService(OnmsMonitoredService svc) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getCollectorNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSchedulerThreads() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Collection getSpecificationsForInterface(OnmsIpInterface iface, String svcName) {
		// TODO Auto-generated method stub
		return null;
	}

}
