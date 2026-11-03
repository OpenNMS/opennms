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
package org.opennms.netmgt.ha;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Who supervises this HA pair.
 */
@XmlEnum
public enum HaMode {
    /**
     * This JVM's {@link HaStartupCoordinator} owns supervision: startup
     * gating, standby monitoring, promotion, failback, split-brain
     * resolution, and config sync.
     */
    @XmlEnumValue("coordinator")
    COORDINATOR,

    /**
     * An external HA agent owns supervision (it writes {@code current_state},
     * {@code active_since} and {@code agent_last_seen}, and starts/stops this
     * service). OpenNMS's only HA job is publishing liveness by writing
     * {@code last_heartbeat}; startup is never gated in this mode.
     */
    @XmlEnumValue("heartbeat-only")
    HEARTBEAT_ONLY
}
