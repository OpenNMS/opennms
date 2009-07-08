/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.snmp.joesnmp;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpVarBind;

public class JoeSnmpV1TrapBuilder implements SnmpV1TrapBuilder {
    
    SnmpPduTrap trap = new SnmpPduTrap();

    public void setEnterprise(SnmpObjId enterpriseId) {
        trap.setEnterprise(new SnmpObjectId(enterpriseId.getIds()));
    }

    public void setAgentAddress(InetAddress agentAddress) {
        trap.setAgentAddress(new SnmpIPAddress(agentAddress));
    }

    public void setGeneric(int generic) {
        trap.setGeneric(generic);
    }

    public void setSpecific(int specific) {
        trap.setSpecific(specific);
    }

    public void setTimeStamp(long timeStamp) {
        trap.setTimeStamp(timeStamp);
    }

    public void send(String destAddr, int destPort, String community) throws Exception {
        JoeSnmpStrategy.send(destAddr, destPort, community, trap);
    }

    public void sendTest(String destAddr, int destPort, String community) throws Exception {
        JoeSnmpStrategy.sendTest(destAddr, destPort, community, trap);
    }

    public void addVarBind(SnmpObjId name, SnmpValue value) {
        SnmpSyntax val = ((JoeSnmpValue) value).getSnmpSyntax();
        trap.addVarBind(new SnmpVarBind(new SnmpObjectId(name.getIds()), val));
    }

}
