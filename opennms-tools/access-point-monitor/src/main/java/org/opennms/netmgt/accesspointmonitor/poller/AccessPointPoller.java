package org.opennms.netmgt.accesspointmonitor.poller;

import java.util.Map;
import java.util.concurrent.Callable;

import org.opennms.netmgt.config.accesspointmonitor.Package;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.model.OnmsAccessPointCollection;
import org.opennms.netmgt.model.OnmsIpInterface;

public interface AccessPointPoller extends Callable<OnmsAccessPointCollection> {

    public void setAccessPointDao(AccessPointDao accessPointDao);

    public AccessPointDao getAccessPointDao();

    public void setInterfaceToPoll(OnmsIpInterface interfaceToPoll);

    public OnmsIpInterface getInterfaceToPoll();

    public void setPropertyMap(Map<String, String> parameters);

    public Map<String, String> getPropertyMap();

    public void setPackage(Package pkg);

    public Package getPackage();

}
