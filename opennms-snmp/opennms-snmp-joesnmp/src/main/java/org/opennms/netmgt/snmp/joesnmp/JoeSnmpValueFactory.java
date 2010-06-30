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
package org.opennms.netmgt.snmp.joesnmp;

import java.math.BigInteger;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.protocols.snmp.SnmpCounter32;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpGauge32;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpNull;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpOpaque;
import org.opennms.protocols.snmp.SnmpTimeTicks;

/**
 * <p>JoeSnmpValueFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JoeSnmpValueFactory implements SnmpValueFactory {

    /**
     * <p>getOctetString</p>
     *
     * @param bytes an array of byte.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getOctetString(byte[] bytes) {
        return new JoeSnmpValue(new SnmpOctetString(bytes));
    }

    /** {@inheritDoc} */
    public SnmpValue getCounter32(long val) {
        return new JoeSnmpValue(new SnmpCounter32(val));
    }

    /** {@inheritDoc} */
    public SnmpValue getCounter64(BigInteger val) {
        return new JoeSnmpValue(new SnmpCounter64(val));
    }

    /** {@inheritDoc} */
    public SnmpValue getGauge32(long val) {;
        return new JoeSnmpValue(new SnmpGauge32(val));
    }

    /** {@inheritDoc} */
    public SnmpValue getInt32(int val) {
        return new JoeSnmpValue(new SnmpInt32(val));
    }

    /** {@inheritDoc} */
    public SnmpValue getIpAddress(InetAddress val) {
        return new JoeSnmpValue(new SnmpIPAddress(val));
    }

    /** {@inheritDoc} */
    public SnmpValue getObjectId(SnmpObjId objId) {
        return new JoeSnmpValue(new SnmpObjectId(objId.getIds()));
    }

    /** {@inheritDoc} */
    public SnmpValue getTimeTicks(long val) {
        return new JoeSnmpValue(new SnmpTimeTicks(val));
    }

    /**
     * <p>getNull</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getNull() {
        return new JoeSnmpValue(new SnmpNull());
    }

    /**
     * <p>getValue</p>
     *
     * @param type a int.
     * @param bytes an array of byte.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getValue(int type, byte[] bytes) {
        return new JoeSnmpValue(type, bytes);
    }

    /**
     * <p>getOpaque</p>
     *
     * @param bs an array of byte.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getOpaque(byte[] bs) {
        return new JoeSnmpValue(new SnmpOpaque(bs));
    }


}
