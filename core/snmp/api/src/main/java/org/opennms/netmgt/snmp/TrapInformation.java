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

import java.io.Serializable;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TrapInformation implements TrapNotification, Serializable {

    private static final long serialVersionUID = -730398590817240290L;

    private static final transient Logger LOG = LoggerFactory.getLogger(TrapInformation.class);

    /**
     * The internet address of the sending agent
     */
    private final InetAddress m_agent;
    /**
     * The community string from the actual SNMP packet
     */
    private final String m_community;
    private TrapProcessor m_trapProcessor;

    protected TrapInformation(InetAddress agent, String community, TrapProcessor trapProcessor) {
        m_agent = agent;
        m_community = community;
        m_trapProcessor = trapProcessor;
        
    }

    protected abstract InetAddress getTrapAddress();

    /**
     * Returns the sending agent's internet address
     */
    protected InetAddress getAgent() {
        return m_agent;
    }

    /**
     * Returns the SNMP community string from the received packet.
     */
    protected String getCommunity() {
        return m_community;
    }

    protected void validate() {
        // by default we do nothing;
    }
    
    protected InetAddress getAgentAddress() {
        return getAgent();
    }

    @Override
    public final TrapProcessor getTrapProcessor() {
        // We do this here to processing of the data is delayed until it is requested.
        return processTrap(this, m_trapProcessor);
    }

    @Override
    public final void setTrapProcessor(TrapProcessor trapProcessor) {
        m_trapProcessor = trapProcessor;
    }

    protected abstract String getVersion();

    protected abstract int getPduLength();

    protected abstract long getTimeStamp();

    protected abstract TrapIdentity getTrapIdentity();

    protected static TrapProcessor processTrap(TrapInformation trap, TrapProcessor trapProcessor) {
        
        trap.validate();
        
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
