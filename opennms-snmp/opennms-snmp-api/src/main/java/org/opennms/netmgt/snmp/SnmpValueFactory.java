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

import java.math.BigInteger;
import java.net.InetAddress;


/**
 * <p>SnmpValueFactory interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface SnmpValueFactory {

    /**
     * <p>getOctetString</p>
     *
     * @param bytes an array of byte.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getOctetString(byte[] bytes);

    /**
     * <p>getCounter32</p>
     *
     * @param val a long.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getCounter32(long val);

    /**
     * <p>getCounter64</p>
     *
     * @param val a {@link java.math.BigInteger} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getCounter64(BigInteger val);

    /**
     * <p>getGauge32</p>
     *
     * @param val a long.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getGauge32(long val);

    /**
     * <p>getInt32</p>
     *
     * @param val a int.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getInt32(int val);

    /**
     * <p>getIpAddress</p>
     *
     * @param val a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getIpAddress(InetAddress val);

    /**
     * <p>getObjectId</p>
     *
     * @param objId a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getObjectId(SnmpObjId objId);

    /**
     * <p>getTimeTicks</p>
     *
     * @param val a long.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getTimeTicks(long val);

    /**
     * <p>getValue</p>
     *
     * @param type a int.
     * @param bytes an array of byte.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getValue(int type, byte[] bytes);

    /**
     * <p>getNull</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getNull();

    /**
     * <p>getOpaque</p>
     *
     * @param bs an array of byte.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    SnmpValue getOpaque(byte[] bs);

}
