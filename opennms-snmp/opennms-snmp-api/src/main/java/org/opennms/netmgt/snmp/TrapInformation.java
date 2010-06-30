//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * <p>Abstract TrapInformation class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class TrapInformation implements TrapNotification {

    /**
     * The internet address of the sending agent
     */
    private InetAddress m_agent;
    /**
     * The community string from the actual SNMP packet
     */
    private String m_community;
    private TrapProcessor m_trapProcessor;

    /**
     * <p>Constructor for TrapInformation.</p>
     *
     * @param agent a {@link java.net.InetAddress} object.
     * @param community a {@link java.lang.String} object.
     * @param trapProcessor a {@link org.opennms.netmgt.snmp.TrapProcessor} object.
     */
    protected TrapInformation(InetAddress agent, String community, TrapProcessor trapProcessor) {
        m_agent = agent;
        m_community = community;
        m_trapProcessor = trapProcessor;
        
    }

    /**
     * <p>getTrapAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    protected abstract InetAddress getTrapAddress();

    /**
     * Returns the sending agent's internet address
     *
     * @return a {@link java.net.InetAddress} object.
     */
    protected InetAddress getAgent() {
        return m_agent;
    }

    /**
     * Returns the SNMP community string from the received packet.
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getCommunity() {
        return m_community;
    }

    /**
     * <p>validate</p>
     */
    protected void validate() {
        // by default we do nothing;
    }
    
    /**
     * <p>log</p>
     *
     * @return a {@link org.apache.log4j.Category} object.
     */
    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>getAgentAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    protected InetAddress getAgentAddress() {
        return getAgent();
    }
    
    /**
     * <p>getTrapProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.TrapProcessor} object.
     */
    public TrapProcessor getTrapProcessor() {
        // We do this here to processing of the data is delayed until it is requested.
        processTrap();
        return m_trapProcessor;
    }

    /**
     * <p>getVersion</p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected abstract String getVersion();

    /**
     * <p>getPduLength</p>
     *
     * @return a int.
     */
    protected abstract int getPduLength();

    /**
     * <p>getTimeStamp</p>
     *
     * @return a long.
     */
    protected abstract long getTimeStamp();

    /**
     * <p>getTrapIdentity</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.TrapIdentity} object.
     */
    protected abstract TrapIdentity getTrapIdentity();

    /**
     * <p>processTrap</p>
     */
    protected void processTrap() {
        
        validate();
        
        m_trapProcessor.setVersion(getVersion());
        m_trapProcessor.setCommunity(getCommunity());
        m_trapProcessor.setAgentAddress(getAgentAddress());
        m_trapProcessor.setTrapAddress(getTrapAddress());
    
        if (log().isDebugEnabled()) {
            log().debug(getVersion()+" trap - trapInterface: " + getTrapAddress());
        }
        
        // time-stamp
        m_trapProcessor.setTimeStamp(getTimeStamp());
    
        m_trapProcessor.setTrapIdentity(getTrapIdentity());
        
        for (int i = 0; i < getPduLength(); i++) {
            processVarBindAt(i);
        } // end for loop
    }

    /**
     * <p>processVarBindAt</p>
     *
     * @param i a int.
     */
    protected abstract void processVarBindAt(int i);

    /**
     * <p>processVarBind</p>
     *
     * @param name a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param value a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    protected void processVarBind(SnmpObjId name, SnmpValue value) {
        m_trapProcessor.processVarBind(name, value);
    }

}
