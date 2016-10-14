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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TrapInformation implements TrapNotification {

    private static final transient Logger LOG = LoggerFactory.getLogger(TrapInformation.class);

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

    private TrapProcessor m_trapProcessor;

    protected TrapInformation(InetAddress agent, String community, TrapProcessor trapProcessor) {
        m_creationTime = new Date().getTime();
        m_agent = agent;
        m_community = community;
        m_trapProcessor = trapProcessor;
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

    protected void validate() {
        // by default we do nothing;
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

    @Override
    public final TrapProcessor getTrapProcessor() {
        // We do this here so that processing of the data is delayed until it is requested.
        return processTrap(this, m_trapProcessor);
    }

    @Override
    public final void setTrapProcessor(final TrapProcessor trapProcessor) {
        m_trapProcessor = trapProcessor;
    }

    public abstract String getVersion();

    public abstract int getPduLength();

    /**
     * Get the SNMP TimeTicks value for the sysUpTime of the agent that
     * generated the trap. Note that the units for this value are 1/100ths
     * of a second instead of milliseconds.
     */
    public abstract long getTimeStamp();

    protected abstract TrapIdentity getTrapIdentity();

    protected static TrapProcessor processTrap(TrapInformation trap, TrapProcessor trapProcessor) {
        
        trap.validate();
        
        trapProcessor.setSystemId(trap.getSystemId());
        trapProcessor.setLocation(trap.getLocation());
        trapProcessor.setCreationTime(trap.getCreationTime());
        trapProcessor.setVersion(trap.getVersion());
        trapProcessor.setCommunity(trap.getCommunity());
        trapProcessor.setAgentAddress(trap.getAgentAddress());
        trapProcessor.setTrapAddress(trap.getTrapAddress());
    
        LOG.debug("{} trap - trapInterface: ()", trap.getVersion(), trap.getTrapAddress());
        
        // time-stamp
        trapProcessor.setTimeStamp(trap.getTimeStamp());
    
        trapProcessor.setTrapIdentity(trap.getTrapIdentity());
        
        for (int i = 0; i < trap.getPduLength(); i++) {
            trap.processVarBindAt(i);
        } // end for loop
    
        return trapProcessor;
    }

    protected abstract void processVarBindAt(int i);

    protected void processVarBind(SnmpObjId name, SnmpValue value) {
        m_trapProcessor.processVarBind(name, value);
    }

}
