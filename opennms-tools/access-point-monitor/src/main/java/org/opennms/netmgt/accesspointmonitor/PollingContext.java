/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.accesspointmonitor;

import java.util.Map;

import org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfig;
import org.opennms.netmgt.config.accesspointmonitor.Package;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventIpcManager;
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
