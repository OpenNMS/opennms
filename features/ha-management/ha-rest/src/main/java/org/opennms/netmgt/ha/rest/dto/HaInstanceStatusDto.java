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

    /** ISO-8601 timestamp at which this instance most recently transitioned into the
     *  ACTIVE state, or {@code null} if it is not ACTIVE. This is the signal used to
     *  resolve split-brain (the instance that became ACTIVE earlier yields). */
    @XmlElement(name = "active-since")
    private String activeSince;

    @XmlElement(name = "hostname")
    private String hostname;

    /** True when this instance's heartbeat age exceeds the failover threshold
     *  (unreachable or unhealthy). Role and state are reported separately. */
    @XmlElement(name = "heartbeat-stale")
    private boolean heartbeatStale;

    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    public String getConfiguredRole() { return configuredRole; }
    public void setConfiguredRole(String configuredRole) { this.configuredRole = configuredRole; }

    public String getCurrentState() { return currentState; }
    public void setCurrentState(String currentState) { this.currentState = currentState; }

    public String getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(String lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }

    public String getActiveSince() { return activeSince; }
    public void setActiveSince(String activeSince) { this.activeSince = activeSince; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    @XmlElement(name = "last-sync-success")
    private String lastSyncSuccess;

    @XmlElement(name = "last-sync-error")
    private String lastSyncError;

    /** Set when sync replaced a file that is only read at JVM startup: this
     * node needs a restart before those values take effect. */
    @XmlElement(name = "restart-required")
    private boolean restartRequired;

    public String getLastSyncSuccess() { return lastSyncSuccess; }
    public void setLastSyncSuccess(String lastSyncSuccess) { this.lastSyncSuccess = lastSyncSuccess; }
    public String getLastSyncError() { return lastSyncError; }
    public void setLastSyncError(String lastSyncError) { this.lastSyncError = lastSyncError; }
    public boolean isRestartRequired() { return restartRequired; }
    public void setRestartRequired(boolean restartRequired) { this.restartRequired = restartRequired; }

    public boolean isHeartbeatStale() { return heartbeatStale; }
    public void setHeartbeatStale(boolean heartbeatStale) { this.heartbeatStale = heartbeatStale; }
}
