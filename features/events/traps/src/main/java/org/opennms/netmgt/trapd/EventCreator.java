/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.snmp.SyntaxToEvent;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapProcessor;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>EventCreator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class EventCreator implements TrapProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventCreator.class);
    
    private final EventBuilder m_eventBuilder;
    private final TrapdIpMgr m_trapdIpMgr;

    
    public EventCreator(TrapdIpMgr trapdIpMgr) {
        m_trapdIpMgr = trapdIpMgr;
        m_eventBuilder = new EventBuilder(null, "trapd");
    }
    
    /** {@inheritDoc} */
    @Override
    public void setCommunity(String community) {
        m_eventBuilder.setCommunity(community);
    }

    /** {@inheritDoc} */
    @Override
    public void setTimeStamp(long timeStamp) {
        m_eventBuilder.setSnmpTimeStamp(timeStamp);
    }

    /** {@inheritDoc} */
    @Override
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
    @Override
    public void setAgentAddress(InetAddress agentAddress) {
        m_eventBuilder.setHost(InetAddressUtils.toIpAddrString(agentAddress));
    }

    /** {@inheritDoc} */
    @Override
    public void processVarBind(SnmpObjId name, SnmpValue value) {
        m_eventBuilder.addParam(SyntaxToEvent.processSyntax(name.toString(), value));
        if (EventConstants.OID_SNMP_IFINDEX.isPrefixOf(name)) {
            m_eventBuilder.setIfIndex(value.toInt());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setTrapAddress(InetAddress trapAddress) {
        m_eventBuilder.setSnmpHost(str(trapAddress));
        m_eventBuilder.setInterface(trapAddress);
        long nodeId = m_trapdIpMgr.getNodeId(str(trapAddress));
        if (nodeId != -1) {
            m_eventBuilder.setNodeid(nodeId);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setTrapIdentity(TrapIdentity trapIdentity) {
        setGeneric(trapIdentity.getGeneric());
        setSpecific(trapIdentity.getSpecific());
        setEnterpriseId(trapIdentity.getEnterpriseId().toString());
    
        LOG.debug("setTrapIdentity: SNMP trap {}", trapIdentity);
    }

    public Event getEvent() {
        return m_eventBuilder.getEvent();
    }

}
