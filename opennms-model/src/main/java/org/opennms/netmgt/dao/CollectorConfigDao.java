package org.opennms.netmgt.dao;

import java.util.Collection;
import java.util.Set;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsPackage;

public interface CollectorConfigDao {

	public abstract OnmsPackage load(String name);

    public abstract OnmsPackage get(String name);
    
    public abstract OnmsPackage findPackageForService(OnmsMonitoredService svc);
    
    //public abstract void save(OnmsPackage pkg);

    //public abstract void update(OnmsPackage pkg);
    
    public abstract Set getCollectorNames();
    
    public abstract int getSchedulerThreads();

	public abstract Collection getSpecificationsForInterface(OnmsIpInterface iface, String svcName);
    
}
