/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TrapInformation implements TrapNotification {
	
	private static final transient Logger LOG = LoggerFactory.getLogger(TrapInformation.class);

    /**
     * The internet address of the sending agent
     */
    private InetAddress m_agent;
    /**
     * The community string from the actual SNMP packet
     */
    private String m_community;
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
    public TrapProcessor getTrapProcessor() {
        // We do this here to processing of the data is delayed until it is requested.
        processTrap();
        return m_trapProcessor;
    }

    protected abstract String getVersion();

    protected abstract int getPduLength();

    protected abstract long getTimeStamp();

    protected abstract TrapIdentity getTrapIdentity();

    protected void processTrap() {
        
        validate();
        
        m_trapProcessor.setVersion(getVersion());
        m_trapProcessor.setCommunity(getCommunity());
        m_trapProcessor.setAgentAddress(getAgentAddress());
        m_trapProcessor.setTrapAddress(getTrapAddress());
    
        LOG.debug("{} trap - trapInterface: ()", getVersion(), getTrapAddress());
        
        // time-stamp
        m_trapProcessor.setTimeStamp(getTimeStamp());
    
        m_trapProcessor.setTrapIdentity(getTrapIdentity());
        
        for (int i = 0; i < getPduLength(); i++) {
            processVarBindAt(i);
        } // end for loop
    }

    protected abstract void processVarBindAt(int i);

    protected void processVarBind(SnmpObjId name, SnmpValue value) {
        m_trapProcessor.processVarBind(name, value);
    }

}
