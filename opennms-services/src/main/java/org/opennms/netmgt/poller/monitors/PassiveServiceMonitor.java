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

import java.util.Map;

import org.opennms.netmgt.passive.PassiveStatusKeeper;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

/**
 * Monitors service status using externally-reported passive status values.
 *
 * <p>On Pollerd, the {@link PassiveStatusKeeper} singleton is populated from
 * {@code passiveServiceStatus} events. On Minion, the singleton is populated
 * by {@code PassiveStatusTwinSubscriber} which receives status via Twin API.</p>
 *
 * <p>This monitor is distributable — it inherits the default
 * {@code getEffectiveLocation()} which passes location through unchanged,
 * allowing it to execute on Minion.</p>
 */
public class PassiveServiceMonitor extends AbstractServiceMonitor {

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        return PassiveStatusKeeper.getInstance().getStatus(svc.getNodeLabel(), svc.getIpAddr(), svc.getSvcName());
    }
}
