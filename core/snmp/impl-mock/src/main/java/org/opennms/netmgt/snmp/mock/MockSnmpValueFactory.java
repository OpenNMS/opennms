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

package org.opennms.netmgt.snmp.mock;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.MockSnmpValue;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;

public class MockSnmpValueFactory implements SnmpValueFactory {
	final Charset m_defaultCharset;
	
	public MockSnmpValueFactory() {
		m_defaultCharset = Charset.defaultCharset();
	}

	@Override
	public SnmpValue getOctetString(final byte[] bytes) {
		return new MockSnmpValue.OctetStringSnmpValue(bytes);
	}

	@Override
	public SnmpValue getCounter32(final long val) {
		return new MockSnmpValue.Counter32SnmpValue(Long.valueOf(val).intValue());
	}

	@Override
	public SnmpValue getCounter64(final BigInteger val) {
		if (val == null) return null;
		return new MockSnmpValue.Counter64SnmpValue(val.longValue());
	}

	@Override
	public SnmpValue getGauge32(final long val) {
		return new MockSnmpValue.Gauge32SnmpValue(Long.valueOf(val).intValue());
	}

	@Override
	public SnmpValue getInt32(final int val) {
		return new MockSnmpValue.Integer32SnmpValue(val);
	}

	@Override
	public SnmpValue getIpAddress(final InetAddress val) {
		if (val == null) return null;
		return new MockSnmpValue.IpAddressSnmpValue(InetAddressUtils.str(val));
	}

	@Override
	public SnmpValue getObjectId(final SnmpObjId objId) {
		return new MockSnmpValue.OidSnmpValue(objId.toString());
	}

	@Override
	public SnmpValue getTimeTicks(final long val) {
		return new MockSnmpValue.TimeticksSnmpValue("(" + val + ")");
	}

	@Override
	public SnmpValue getValue(int type, byte[] bytes) {
		if (bytes == null) return null;
		final ByteBuffer bb = ByteBuffer.allocate(bytes.length);
		bb.put(bytes);
		bb.flip();
		final String value = m_defaultCharset.decode(bb).toString();
		switch (type) {
			case SnmpValue.SNMP_COUNTER32:
				return SnmpUtils.parseMibValue("Counter32: " + value);
			case SnmpValue.SNMP_COUNTER64:
				return SnmpUtils.parseMibValue("Counter64: " + value);
			case SnmpValue.SNMP_END_OF_MIB:
				return MockSnmpValue.END_OF_MIB;
			case SnmpValue.SNMP_GAUGE32:
				return SnmpUtils.parseMibValue("Gauge32: " + value);
			case SnmpValue.SNMP_INT32:
				return SnmpUtils.parseMibValue("INTEGER: " + value);
			case SnmpValue.SNMP_IPADDRESS:
				return SnmpUtils.parseMibValue("IpAddress: " + value);
			case SnmpValue.SNMP_NO_SUCH_INSTANCE:
				return MockSnmpValue.NO_SUCH_INSTANCE;
			case SnmpValue.SNMP_NO_SUCH_OBJECT:
				return MockSnmpValue.NO_SUCH_OBJECT;
			case SnmpValue.SNMP_NULL:
				return MockSnmpValue.NULL_VALUE;
			case SnmpValue.SNMP_OBJECT_IDENTIFIER:
				return SnmpUtils.parseMibValue("OID: " + value);
			case SnmpValue.SNMP_OCTET_STRING:
				return SnmpUtils.parseMibValue("Hex-STRING: " + value);
			case SnmpValue.SNMP_TIMETICKS:
				return SnmpUtils.parseMibValue("Timeticks: " + value);
			case SnmpValue.SNMP_OPAQUE:
				throw new IllegalArgumentException("Unable to handle opaque types in MockSnmpValue");
			default:
		        throw new IllegalArgumentException("Unknown SNMP value type: "+type);
		}
	}

	@Override
	public SnmpValue getNull() {
		return MockSnmpValue.NULL_VALUE;
	}

	@Override
	public SnmpValue getOpaque(final byte[] bs) {
		throw new IllegalArgumentException("Unable to handle opaque types in MockSnmpValue");
	}

}
