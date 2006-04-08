package org.opennms.netmgt.dao;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsPackage;

public interface CollectorConfigDao {

	public abstract OnmsPackage load(String name);

    public abstract OnmsPackage get(String name);
    
    public abstract OnmsPackage findPackageForService(OnmsMonitoredService svc);
    
    //public abstract void save(OnmsPackage pkg);

    //public abstract void update(OnmsPackage pkg);

}
