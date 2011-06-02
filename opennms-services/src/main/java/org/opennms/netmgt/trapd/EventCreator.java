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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.snmp.SyntaxToEvent;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>EventCreator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class EventCreator implements TrapProcessor {
    
    private EventBuilder m_eventBuilder;
    private TrapdIpMgr m_trapdIpMgr;

    
    EventCreator(TrapdIpMgr trapdIpMgr) {
        m_trapdIpMgr = trapdIpMgr;
        
        m_eventBuilder = new EventBuilder(null, "trapd");
    }
    
    /** {@inheritDoc} */
    public void setCommunity(String community) {
        m_eventBuilder.setCommunity(community);
    }

    /** {@inheritDoc} */
    public void setTimeStamp(long timeStamp) {
        m_eventBuilder.setSnmpTimeStamp(timeStamp);
    }

    /** {@inheritDoc} */
    public void setVersion(String version) {
        m_eventBuilder.setSnmpVersion(version);
    }

    private void setGeneric(int generic) {
        m_eventBuilder.setGeneric(generic);
    }

    private void setSpecific(int specific) {
        m_eventBuilder.setSpecific(specific);
    }

    private void setEnterpriseId(String enterpriseId) {
        m_eventBuilder.setEnterpriseId(enterpriseId);
    }

    /** {@inheritDoc} */
    public void setAgentAddress(InetAddress agentAddress) {
        m_eventBuilder.setHost(InetAddressUtils.toIpAddrString(agentAddress));
    }

    /** {@inheritDoc} */
    public void processVarBind(SnmpObjId name, SnmpValue value) {
        m_eventBuilder.addParam(SyntaxToEvent.processSyntax(name.toString(), value));
        if (EventConstants.OID_SNMP_IFINDEX.isPrefixOf(name)) {
            m_eventBuilder.setIfIndex(value.toInt());
        }
    }

    /** {@inheritDoc} */
    public void setTrapAddress(InetAddress trapAddress) {
        m_eventBuilder.setSnmpHost(str(trapAddress));
        m_eventBuilder.setInterface(trapAddress);
        long nodeId = m_trapdIpMgr.getNodeId(str(trapAddress));
        if (nodeId != -1) {
            m_eventBuilder.setNodeid(nodeId);
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
        return m_eventBuilder.getEvent();
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
