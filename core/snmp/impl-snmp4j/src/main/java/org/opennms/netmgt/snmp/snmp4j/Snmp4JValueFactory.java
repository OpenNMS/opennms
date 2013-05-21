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

public class Snmp4JValueFactory implements SnmpValueFactory {

    @Override
    public SnmpValue getOctetString(byte[] bytes) {
        return new Snmp4JValue(new OctetString(bytes));
    }

    @Override
    public SnmpValue getCounter32(long val) {
        return new Snmp4JValue(new Counter32(val));
    }

    @Override
    public SnmpValue getCounter64(BigInteger bigInt) {
        return new Snmp4JValue(new Counter64(bigInt.longValue()));
    }

    @Override
    public SnmpValue getGauge32(long val) {
        return new Snmp4JValue(new Gauge32(val));
    }

    @Override
    public SnmpValue getInt32(int val) {
        return new Snmp4JValue(new Integer32(val));
    }

    @Override
    public SnmpValue getIpAddress(InetAddress val) {
        return new Snmp4JValue(new IpAddress(val));
    }

    @Override
    public SnmpValue getObjectId(SnmpObjId objId) {
        return new Snmp4JValue(new OID(objId.getIds()));
    }

    @Override
    public SnmpValue getTimeTicks(long val) {
        return new Snmp4JValue(new TimeTicks(val));
    }

    @Override
    public SnmpValue getNull() {
        return new Snmp4JValue(new Null());
    }

    @Override
    public SnmpValue getValue(int type, byte[] bytes) {
        return new Snmp4JValue(type, bytes);
    }

    @Override
    public SnmpValue getOpaque(byte[] bs) {
        return new Snmp4JValue(new Opaque(bs));
    }


}
