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

import org.opennms.netmgt.snmp.SnmpValue;

/**
 * <p>TrapProcessor interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface TrapProcessor {

    /**
     * <p>setCommunity</p>
     *
     * @param community a {@link java.lang.String} object.
     */
    public abstract void setCommunity(String community);

    /**
     * <p>setTimeStamp</p>
     *
     * @param timeStamp a long.
     */
    public abstract void setTimeStamp(long timeStamp);

    /**
     * <p>setVersion</p>
     *
     * @param version a {@link java.lang.String} object.
     */
    public abstract void setVersion(String version);

    /**
     * <p>setAgentAddress</p>
     *
     * @param agentAddress a {@link java.net.InetAddress} object.
     */
    public abstract void setAgentAddress(InetAddress agentAddress);

    /**
     * <p>processVarBind</p>
     *
     * @param name a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param value a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public abstract void processVarBind(SnmpObjId name, SnmpValue value);

    /**
     * <p>setTrapAddress</p>
     *
     * @param trapAddress a {@link java.net.InetAddress} object.
     */
    public abstract void setTrapAddress(InetAddress trapAddress);

    /**
     * <p>setTrapIdentity</p>
     *
     * @param trapIdentity a {@link org.opennms.netmgt.snmp.TrapIdentity} object.
     */
    public abstract void setTrapIdentity(TrapIdentity trapIdentity);

}
