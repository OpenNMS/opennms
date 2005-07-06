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
package org.opennms.netmgt.trapd;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.protocols.ip.IPv4Address;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

public abstract class TrapInformation {

    /**
     * The internet address of the sending agent
     */
    private InetAddress m_agent;
    /**
     * The community string from the actual SNMP packet
     */
    private String m_community;
    protected Event m_event;
    private Snmp m_snmpInfo;
    protected Parms m_parms;

    protected TrapInformation(InetAddress agent, String community) {
        m_agent = agent;
        m_community = community;
        
        m_event = new Event();
        m_event.setSource("trapd");
        m_event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));

        m_snmpInfo = new Snmp();
        m_event.setSnmp(m_snmpInfo);

        m_parms = new Parms();
        m_event.setParms(m_parms);
    }

    protected abstract String getTrapInterface();

    /**
     * Returns the sending agent's internet address
     */
    public InetAddress getAgent() {
        return m_agent;
    }

    /**
     * Returns the SNMP community string from the received packet.
     */
    public String getCommunity() {
        return m_community;
    }

    protected void setTimeStamp(long timeStamp) {
        m_snmpInfo.setTimeStamp(timeStamp);
    }

    protected void setCommunity(String community) {
        m_snmpInfo.setCommunity(community);
    }

    protected void setTrapIdentity(TrapIdentity trapIdentity) {
        setGeneric(trapIdentity.getGeneric());
        setSpecific(trapIdentity.getSpecific());
        setEnterpriseId(trapIdentity.getEnterpriseId());
    
        if (log().isDebugEnabled()) {
            log().debug("snmp specific/generic/eid: " + m_snmpInfo.getSpecific() + "/" + m_snmpInfo.getGeneric() + "/" + m_snmpInfo.getId());
        }
    
    
    }

    protected void setVersion(String version) {
        m_snmpInfo.setVersion(version);
    }

    private void setGeneric(int generic) {
        m_snmpInfo.setGeneric(generic);
    }

    private void setSpecific(int specific) {
        m_snmpInfo.setSpecific(specific);
    }

    private void setEnterpriseId(String enterpriseId) {
        m_snmpInfo.setId(enterpriseId);
    }

    protected void setTrapAddress(String trapInterface) {
        m_event.setSnmphost(trapInterface);
        m_event.setInterface(trapInterface);
        if (getNodeId(trapInterface) != -1)
            m_event.setNodeid(getNodeId(trapInterface));
    }

    protected void setAgentAddress(String agentAddress) {
        m_event.setHost(agentAddress);
    }

    protected void validate() {
        // TODO Auto-generated method stub
        
    }
    
    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    protected void processVarBind(TrapQueueProcessor processor, SnmpObjectId name, SnmpSyntax obj) {
        m_parms.addParm(processor.processSyntax(name.toString(), obj));
    }

    protected String getAgentAddress() {
        InetAddress agent = getAgent();
        String agentAddr = new IPv4Address(agent).toString();
        return agentAddr;
    }

    public Event getEventForTrap(TrapQueueProcessor processor) {
        processTrap(processor);
        return m_event;
    }

    public long getNodeId(String trapInterface) {
        
        // FIXME: cache this nodeId once it is computed
        String ipNodeId = TrapdIPMgr.getNodeId(trapInterface);
        long nodeId = -1L;
        if (ipNodeId != null) {
            nodeId = Long.parseLong(ipNodeId);
        }
        return nodeId;
    }

    protected abstract String getVersion();

    protected abstract int getPduLength();

    protected abstract SnmpVarBind getVarBindAt(int index);

    protected abstract long getTimeStamp();

    protected abstract TrapIdentity getTrapIdentity();

    protected void processTrap(TrapQueueProcessor processor) {
        
        validate();
        
        setVersion(getVersion());
        setCommunity(getCommunity());
        setAgentAddress(getAgentAddress());
        setTrapAddress(getTrapInterface());
    
        if (log().isDebugEnabled()) {
            log().debug(getVersion()+" trap - trapInterface: " + getTrapInterface());
        }
        
        // time-stamp
        setTimeStamp(getTimeStamp());
    
        setTrapIdentity(getTrapIdentity());
        
        for (int i = 0; i < getPduLength(); i++) {
            SnmpObjectId name = getVarBindAt(i).getName();
            SnmpSyntax obj = getVarBindAt(i).getValue();
            
            processVarBind(processor, name, obj);
        } // end for loop
    }

}
