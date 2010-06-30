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
package org.opennms.netmgt.snmp.snmp4j;

import java.math.BigInteger;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Opaque;
import org.snmp4j.smi.TimeTicks;

/**
 * <p>Snmp4JValueFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Snmp4JValueFactory implements SnmpValueFactory {

    /**
     * <p>getOctetString</p>
     *
     * @param bytes an array of byte.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getOctetString(byte[] bytes) {
        return new Snmp4JValue(new OctetString(bytes));
    }

    /** {@inheritDoc} */
    public SnmpValue getCounter32(long val) {
        return new Snmp4JValue(new Counter32(val));
    }

    /** {@inheritDoc} */
    public SnmpValue getCounter64(BigInteger bigInt) {
        return new Snmp4JValue(new Counter64(bigInt.longValue()));
    }

    /** {@inheritDoc} */
    public SnmpValue getGauge32(long val) {
        return new Snmp4JValue(new Gauge32(val));
    }

    /** {@inheritDoc} */
    public SnmpValue getInt32(int val) {
        return new Snmp4JValue(new Integer32(val));
    }

    /** {@inheritDoc} */
    public SnmpValue getIpAddress(InetAddress val) {
        return new Snmp4JValue(new IpAddress(val));
    }

    /** {@inheritDoc} */
    public SnmpValue getObjectId(SnmpObjId objId) {
        return new Snmp4JValue(new OID(objId.getIds()));
    }

    /** {@inheritDoc} */
    public SnmpValue getTimeTicks(long val) {
        return new Snmp4JValue(new TimeTicks(val));
    }

    /**
     * <p>getNull</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getNull() {
        return new Snmp4JValue(new Null());
    }

    /**
     * <p>getValue</p>
     *
     * @param type a int.
     * @param bytes an array of byte.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getValue(int type, byte[] bytes) {
        return new Snmp4JValue(type, bytes);
    }

    /**
     * <p>getOpaque</p>
     *
     * @param bs an array of byte.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue getOpaque(byte[] bs) {
        return new Snmp4JValue(new Opaque(bs));
    }


}
