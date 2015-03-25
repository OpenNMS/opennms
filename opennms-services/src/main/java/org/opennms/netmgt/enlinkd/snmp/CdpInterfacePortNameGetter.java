/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdpInterfacePortNameGetter extends TableTracker {

    public final static SnmpObjId CDP_INTERFACE_NAME = SnmpObjId.get(".1.3.6.1.4.1.9.9.23.1.1.1.1.6");

	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
    private SnmpAgentConfig m_agentConfig;
    private static final Logger LOG = LoggerFactory.getLogger(CdpInterfacePortNameGetter.class);

    public CdpInterfacePortNameGetter(SnmpAgentConfig peer) {
        m_agentConfig = peer;
    }

    public CdpLink get(CdpLink link) {
        SnmpObjId instance = SnmpObjId.get(new int[] {link.getCdpCacheIfIndex()});
        SnmpObjId[] oids = new SnmpObjId[]{SnmpObjId.get(CDP_INTERFACE_NAME, instance)};

        SnmpValue[] val = SnmpUtils.get(m_agentConfig, oids);
        LOG.info("get: oid '{}' found value '{}'", oids[0], val);
        if (val == null || val.length != 1 || val[0] == null) 
            return link;
        link.setCdpInterfaceName(val[0].toDisplayString());
        return link;
    }

}
