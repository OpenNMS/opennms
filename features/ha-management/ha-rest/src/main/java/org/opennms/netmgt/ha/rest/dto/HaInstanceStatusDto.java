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
package org.opennms.netmgt.ha.rest.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

@XmlRootElement(name = "ha-instance-status")
@XmlAccessorType(XmlAccessType.NONE)
public class HaInstanceStatusDto {

    @XmlElement(name = "instance-id")
    private String instanceId;

    @XmlElement(name = "configured-role")
    private String configuredRole;

    @XmlElement(name = "current-state")
    private String currentState;

    @XmlElement(name = "last-heartbeat")
    private String lastHeartbeat;

    @XmlElement(name = "hostname")
    private String hostname;

    /** True when the cluster is in a degraded state: SECONDARY is ACTIVE (failover occurred),
     *  or PRIMARY is in DEGRADED state (waiting for failback). */
    @XmlElement(name = "degraded")
    private boolean degraded;

    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    public String getConfiguredRole() { return configuredRole; }
    public void setConfiguredRole(String configuredRole) { this.configuredRole = configuredRole; }

    public String getCurrentState() { return currentState; }
    public void setCurrentState(String currentState) { this.currentState = currentState; }

    public String getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(String lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public boolean isDegraded() { return degraded; }
    public void setDegraded(boolean degraded) { this.degraded = degraded; }
}
