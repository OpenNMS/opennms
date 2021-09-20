/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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
