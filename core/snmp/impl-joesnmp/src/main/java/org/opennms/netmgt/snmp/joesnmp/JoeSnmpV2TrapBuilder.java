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

package org.opennms.netmgt.snmp.joesnmp;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

public class JoeSnmpV2TrapBuilder implements SnmpTrapBuilder {

    SnmpPduRequest m_pdu;
    
    public JoeSnmpV2TrapBuilder() {
        m_pdu = new SnmpPduRequest(SnmpPduPacket.V2TRAP);
        m_pdu.setRequestId(SnmpPduPacket.nextSequence());
    }
    
    @Override
    public void send(String destAddr, int destPort, String community) throws Exception {
        JoeSnmpStrategy.send(destAddr, destPort, community, m_pdu);
    }

    @Override
    public void sendTest(String destAddr, int destPort, String community) throws Exception {
        JoeSnmpStrategy.sendTest(destAddr, destPort, community, m_pdu);
    }
    
    @Override
    public void addVarBind(SnmpObjId name, SnmpValue value) {
        SnmpSyntax val = ((JoeSnmpValue) value).getSnmpSyntax();
        m_pdu.addVarBind(new SnmpVarBind(new SnmpObjectId(name.getIds()), val));
    }


}
