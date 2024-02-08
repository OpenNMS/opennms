/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

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
