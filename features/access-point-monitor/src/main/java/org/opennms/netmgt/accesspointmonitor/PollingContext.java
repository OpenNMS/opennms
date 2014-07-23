package org.opennms.netmgt.accesspointmonitor;

import java.util.Map;

import org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfig;
import org.opennms.netmgt.config.accesspointmonitor.Package;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;

public interface PollingContext extends ReadyRunnable {

    void init();

    void release();

    void setPackage(Package pkg);

    Package getPackage();

    void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao);

    IpInterfaceDao getIpInterfaceDao();

    NodeDao getNodeDao();

    void setNodeDao(NodeDao nodeDao);

    void setAccessPointDao(AccessPointDao accessPointDao);

    AccessPointDao getAccessPointDao();

    void setEventManager(EventIpcManager eventMgr);

    EventIpcManager getEventManager();

    void setScheduler(Scheduler scheduler);

    Scheduler getScheduler();

    void setPollerConfig(AccessPointMonitorConfig accesspointmonitorConfig);

    AccessPointMonitorConfig getPollerConfig();

    void setInterval(long interval);

    long getInterval();

    void setPropertyMap(Map<String, String> parameters);

    Map<String, String> getPropertyMap();

}
