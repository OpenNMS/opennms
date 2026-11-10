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
package org.opennms.netmgt.snmp;

import java.net.InetAddress;
import java.util.Date;

public abstract class TrapInformation {

    /**
     * The internet address of the sending agent.
     */
    private final InetAddress m_agent;

    /**
     * The community string from the actual SNMP packet.
     */
    private final String m_community;

    /**
     * The initial creation time of this object. This is used to track the reception
     * time of the event.
     */
    private long m_creationTime;

    /**
     * Optional system ID of the monitoring system that received this trap
     */
    private String systemId;

    /**
     * Optional location of the monitoring system that received this trap
     */
    private String location;

    protected TrapInformation(InetAddress agent, String community) {
        m_creationTime = new Date().getTime();
        m_agent = agent;
        m_community = community;
    }

    /**
     * @return The source IP address of the trap. For SNMPv2 traps, this value
     * is always the same as the value of {@link #getAgentAddress()} but for SNMPv1
     * traps, the value can be different if the trap has been forwarded. It then
     * represents the true source IP address of the trap event.
     */
    public abstract InetAddress getTrapAddress();

    public final String getSystemId() {
        return systemId;
    }

    public final void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public final String getLocation() {
        return location;
    }

    public final void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the SNMP community string from the received packet.
     */
    public final String getCommunity() {
        return m_community;
    }

    /**
     * Validate the trap.
     *
     * @throws SnmpException on validation error.
     */
    public void validate() throws SnmpException {
        // by default we do nothing
    }

    /**
     * Returns the sending agent's internet address
     */
    public final InetAddress getAgentAddress() {
        return m_agent;
    }

    public final long getCreationTime() {
        return m_creationTime;
    }

    public final void setCreationTime(long creationTime) {
        m_creationTime = creationTime;
    }

    public abstract String getVersion();

    public abstract int getPduLength();

    /**
     * Get the SNMP TimeTicks value for the sysUpTime of the agent that
     * generated the trap. Note that the units for this value are 1/100ths
     * of a second instead of milliseconds.
     */
    public abstract long getTimeStamp();

    public abstract TrapIdentity getTrapIdentity();

    protected abstract Integer getRequestId();

    public abstract SnmpVarBindDTO getSnmpVarBindDTO(int i);

}
