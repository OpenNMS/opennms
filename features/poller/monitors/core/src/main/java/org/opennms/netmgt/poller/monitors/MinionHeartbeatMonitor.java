/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.lang.management.ManagementFactory;
import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

@Distributable(DistributionContext.DAEMON)
public class MinionHeartbeatMonitor extends AbstractServiceMonitor {

    private final Supplier<NodeDao> nodeDao = Suppliers.memoize(() -> BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class));
    private final Supplier<MinionDao> minionDao = Suppliers.memoize(() -> BeanUtils.getBean("daoContext", "minionDao", MinionDao.class));

    @Override
    public PollStatus poll(final MonitoredService svc, final Map<String, Object> parameters) {
        // Minions send heartbeat every 30 seconds - we check that we can skip not more than one beat
        final int period = 2 * ParameterMap.getKeyedInteger(parameters, "period", 30 * 1000);

        // Get the minion to test whereas the minion ID is the nodes foreign ID by convention
        final OnmsNode node = nodeDao.get().get(svc.getNodeId());
        final OnmsMinion minion = minionDao.get().findById(node.getForeignId());

        // Calculate the time since the last heartbeat was received
        final long lastSeen = System.currentTimeMillis() - minion.getLastUpdated().getTime();

        final PollStatus status;
        if (lastSeen <= period) {
            status = PollStatus.available();
        } else if (ManagementFactory.getRuntimeMXBean().getUptime() < period) {
            status = PollStatus.unknown("JVM has not been started long enough to process a heartbeat.");
        } else {
            status = PollStatus.unavailable(String.format("Last heartbeat was %.2f seconds ago", lastSeen / 1000.0));
        }

        return status;
    }

    @Override
    public String getEffectiveLocation(String location) {
        // Always run in the OpenNMS JVM
        return MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;
    }
}
