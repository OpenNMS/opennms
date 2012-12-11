package org.opennms.netmgt.accesspointmonitor;

import java.util.Map;

import org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfig;
import org.opennms.netmgt.config.accesspointmonitor.Package;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;

public interface PollingContext extends ReadyRunnable {

    public void init();

    public void release();

    public void setPackage(Package pkg);

    public Package getPackage();

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao);

    public IpInterfaceDao getIpInterfaceDao();

    public NodeDao getNodeDao();

    public void setNodeDao(NodeDao nodeDao);

    public void setAccessPointDao(AccessPointDao accessPointDao);

    public AccessPointDao getAccessPointDao();

    public void setEventManager(EventIpcManager eventMgr);

    public EventIpcManager getEventManager();

    public void setScheduler(Scheduler scheduler);

    public Scheduler getScheduler();

    public void setPollerConfig(AccessPointMonitorConfig accesspointmonitorConfig);

    public AccessPointMonitorConfig getPollerConfig();

    public void setInterval(long interval);

    public long getInterval();

    public void setPropertyMap(Map<String, String> parameters);

    public Map<String, String> getPropertyMap();

}
