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
// Modifications:
//
// 2008 Jan 26: Dependency inject TrapdIpMgr. - dj@opennms.org
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

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Snmp;

/**
 * <p>EventCreator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class EventCreator implements TrapProcessor {
    
    private Event m_event;
    private Snmp m_snmpInfo;
    private Parms m_parms;
    private TrapdIpMgr m_trapdIpMgr;

    
    EventCreator(TrapdIpMgr trapdIpMgr) {
        m_trapdIpMgr = trapdIpMgr;
        
        m_event = new Event();
        m_event.setSource("trapd");
        m_event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));

        m_snmpInfo = new Snmp();
        m_event.setSnmp(m_snmpInfo);

        m_parms = new Parms();
        m_event.setParms(m_parms);
    }
    
    /** {@inheritDoc} */
    public void setCommunity(String community) {
        m_snmpInfo.setCommunity(community);
    }

    /** {@inheritDoc} */
    public void setTimeStamp(long timeStamp) {
        m_snmpInfo.setTimeStamp(timeStamp);
    }

    /** {@inheritDoc} */
    public void setVersion(String version) {
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

    /** {@inheritDoc} */
    public void setAgentAddress(InetAddress agentAddress) {
        m_event.setHost(agentAddress.getHostAddress());
    }

    /** {@inheritDoc} */
    public void processVarBind(SnmpObjId name, SnmpValue value) {
        m_parms.addParm(SyntaxToEvent.processSyntax(name.toString(), value));
        if (EventConstants.OID_SNMP_IFINDEX.isPrefixOf(name)) {
            m_event.setIfIndex(value.toInt());
        }
    }

    /** {@inheritDoc} */
    public void setTrapAddress(InetAddress trapAddress) {
        String trapInterface = trapAddress.getHostAddress();
        m_event.setSnmphost(trapInterface);
        m_event.setInterface(trapInterface);
        long nodeId = m_trapdIpMgr.getNodeId(trapInterface);
        if (nodeId != -1) {
            m_event.setNodeid(nodeId);
        }
    }

    /** {@inheritDoc} */
    public void setTrapIdentity(TrapIdentity trapIdentity) {
        setGeneric(trapIdentity.getGeneric());
        setSpecific(trapIdentity.getSpecific());
        setEnterpriseId(trapIdentity.getEnterpriseId().toString());
    
        if (log().isDebugEnabled()) {
            log().debug("setTrapIdentity: snmp trap "+trapIdentity);
        }
    
    }

    Event getEvent() {
        return m_event;
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
