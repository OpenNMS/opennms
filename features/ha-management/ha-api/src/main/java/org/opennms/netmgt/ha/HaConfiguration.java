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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * JAXB model for {@code $OPENNMS_HOME/etc/ha-configuration.xml}.
 *
 * <p>Example:
 * <pre>{@code
 * <ha-configuration>
 *   <enabled>true</enabled>
 *   <instance-id>opennms-primary</instance-id>
 *   <role>PRIMARY</role>
 *   <partner-instance-id>opennms-secondary</partner-instance-id>
 *   <heartbeat-interval-seconds>10</heartbeat-interval-seconds>
 *   <failover-threshold-seconds>30</failover-threshold-seconds>
 * </ha-configuration>
 * }</pre>
 */
@XmlRootElement(name = "ha-configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class HaConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "enabled", defaultValue = "false")
    private boolean enabled = false;

    @XmlElement(name = "instance-id", required = true)
    private String instanceId;

    @XmlElement(name = "role", required = true)
    private HaRole role;

    @XmlElement(name = "partner-instance-id")
    private String partnerInstanceId;

    /** How often PRIMARY writes heartbeat to DB. Default: 10 seconds. */
    @XmlElement(name = "heartbeat-interval-seconds")
    private int heartbeatIntervalSeconds = 10;

    /**
     * How stale PRIMARY heartbeat must be before SECONDARY promotes.
     * Must be greater than {@code heartbeatIntervalSeconds}. Default: 30 seconds.
     */
    @XmlElement(name = "failover-threshold-seconds")
    private int failoverThresholdSeconds = 30;

    /**
     * Base REST URL of the partner instance used for config synchronization.
     * Example: {@code http://opennms-primary:8980/opennms}
     * Required when {@code sync-enabled} is true.
     */
    @XmlElement(name = "partner-rest-url")
    private String partnerRestUrl;

    /** Whether to periodically sync configuration from the partner while in STANDBY. Default: true. */
    @XmlElement(name = "sync-enabled")
    private boolean syncEnabled = true;

    /** How often to fetch configuration from the partner while in STANDBY (seconds). Default: 300. */
    @XmlElement(name = "sync-interval-seconds")
    private int syncIntervalSeconds = 300;

    /**
     * Username of the service account used for config sync REST calls.
     * This account must have {@code ROLE_FILESYSTEM_EDITOR} on the partner instance.
     * Default: {@code hasync}.
     */
    @XmlElement(name = "sync-username")
    private String syncUsername = "hasync";

    /**
     * Password for the sync service account. May be a literal value or an SCV expression
     * of the form {@code ${scv:alias:attribute}}.
     * Example: {@code ${scv:hasync:password}}
     */
    @XmlElement(name = "sync-password")
    private String syncPassword;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public HaRole getRole() {
        return role;
    }

    public void setRole(HaRole role) {
        this.role = role;
    }

    public String getPartnerInstanceId() {
        return partnerInstanceId;
    }

    public void setPartnerInstanceId(String partnerInstanceId) {
        this.partnerInstanceId = partnerInstanceId;
    }

    public int getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public void setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public int getFailoverThresholdSeconds() {
        return failoverThresholdSeconds;
    }

    public void setFailoverThresholdSeconds(int failoverThresholdSeconds) {
        this.failoverThresholdSeconds = failoverThresholdSeconds;
    }

    public String getPartnerRestUrl() {
        return partnerRestUrl;
    }

    public void setPartnerRestUrl(String partnerRestUrl) {
        this.partnerRestUrl = partnerRestUrl;
    }

    public boolean isSyncEnabled() {
        return syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }

    public int getSyncIntervalSeconds() {
        return syncIntervalSeconds;
    }

    public void setSyncIntervalSeconds(int syncIntervalSeconds) {
        this.syncIntervalSeconds = syncIntervalSeconds;
    }

    public String getSyncUsername() {
        return syncUsername;
    }

    public void setSyncUsername(String syncUsername) {
        this.syncUsername = syncUsername;
    }

    public String getSyncPassword() {
        return syncPassword;
    }

    public void setSyncPassword(String syncPassword) {
        this.syncPassword = syncPassword;
    }
}
