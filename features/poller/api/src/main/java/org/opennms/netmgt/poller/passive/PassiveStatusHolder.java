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
package org.opennms.netmgt.poller.passive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.netmgt.poller.PollStatus;

/**
 * Thread-safe static holder for passive service status.
 *
 * <p>Written to by {@code PassiveStatusKeeper} (on Pollerd) or
 * {@code PassiveStatusTwinSubscriber} (on Minion).
 * Read by {@code PassiveServiceMonitor} on whichever side it executes.</p>
 */
public class PassiveStatusHolder {

    private static final Map<PassiveStatusKey, PollStatus> STATUS_MAP = new ConcurrentHashMap<>();

    public static PollStatus getStatus(String nodeLabel, String ipAddr, String svcName) {
        PollStatus status = STATUS_MAP.get(new PassiveStatusKey(nodeLabel, ipAddr, svcName));
        return status != null ? status : PollStatus.up();
    }

    public static void setStatus(String nodeLabel, String ipAddr, String svcName, PollStatus status) {
        STATUS_MAP.put(new PassiveStatusKey(nodeLabel, ipAddr, svcName), status);
    }

    public static void updateFromConfig(PassiveStatusConfig config) {
        Map<PassiveStatusKey, PollStatus> newMap = config.toStatusMap();
        STATUS_MAP.clear();
        STATUS_MAP.putAll(newMap);
    }
}
