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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.snmp.InetAddrUtils;
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
		return new MockSnmpValue.Counter32SnmpValue(val);
	}

	@Override
	public SnmpValue getCounter64(final BigInteger val) {
		if (val == null) return null;
		return new MockSnmpValue.Counter64SnmpValue(val);
	}

	@Override
	public SnmpValue getGauge32(final long val) {
		return new MockSnmpValue.Gauge32SnmpValue(val);
	}

	@Override
	public SnmpValue getInt32(final int val) {
		return new MockSnmpValue.Integer32SnmpValue(val);
	}

	@Override
	public SnmpValue getIpAddress(final InetAddress val) {
		if (val == null) return null;
		return new MockSnmpValue.IpAddressSnmpValue(val);
	}

	@Override
	public SnmpValue getObjectId(final SnmpObjId objId) {
		return new MockSnmpValue.OidSnmpValue(objId);
	}

	@Override
	public SnmpValue getTimeTicks(final long val) {
		return new MockSnmpValue.TimeticksSnmpValue(val);
	}

	@Override
	public SnmpValue getValue(int type, byte[] bytes) {
		switch (type) {
			case SnmpValue.SNMP_COUNTER32:
				return new MockSnmpValue.Counter32SnmpValue(new BigInteger(bytes).longValue());
			case SnmpValue.SNMP_COUNTER64:
				return new MockSnmpValue.Counter64SnmpValue(new BigInteger(bytes));
			case SnmpValue.SNMP_END_OF_MIB:
				return MockSnmpValue.END_OF_MIB;
			case SnmpValue.SNMP_GAUGE32:
				return new MockSnmpValue.Gauge32SnmpValue(new BigInteger(bytes).longValue());
			case SnmpValue.SNMP_INT32:
				return new MockSnmpValue.Integer32SnmpValue(new BigInteger(bytes).intValue());
			case SnmpValue.SNMP_IPADDRESS:
				return new MockSnmpValue.IpAddressSnmpValue(bytes);
			case SnmpValue.SNMP_NO_SUCH_INSTANCE:
				return MockSnmpValue.NO_SUCH_INSTANCE;
			case SnmpValue.SNMP_NO_SUCH_OBJECT:
				return MockSnmpValue.NO_SUCH_OBJECT;
			case SnmpValue.SNMP_NULL:
				return MockSnmpValue.NULL_VALUE;
			case SnmpValue.SNMP_OBJECT_IDENTIFIER:
				return new MockSnmpValue.OidSnmpValue(new String(bytes));
			case SnmpValue.SNMP_OCTET_STRING:
				return new MockSnmpValue.OctetStringSnmpValue(bytes);
			case SnmpValue.SNMP_TIMETICKS:
				return new MockSnmpValue.TimeticksSnmpValue(new BigInteger(bytes).longValue());
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
	
	private static final Pattern HEX_CHUNK_PATTERN = Pattern.compile("(..)[ :]?");

	SnmpValue parseMibValue(final String mibVal) {
		if (mibVal.startsWith("OID:")) {
	    	return getObjectId(SnmpObjId.get(mibVal.substring("OID:".length()).trim()));
	    } else if (mibVal.startsWith("Timeticks:")) {
	    	String timeticks = mibVal.substring("Timeticks:".length()).trim();
			if (timeticks.contains("(")) {
				timeticks = timeticks.replaceAll("^.*\\((\\d*?)\\).*$", "$1");
			}
			return getTimeTicks(Long.valueOf(timeticks));
		} else if (mibVal.startsWith("STRING:")) {
			return getOctetString(mibVal.substring("STRING:".length()).trim().getBytes());
		} else if (mibVal.startsWith("INTEGER:")) {
			return getInt32(Integer.valueOf(mibVal.substring("INTEGER:".length()).trim().replaceAll(" *.[Bb]ytes$", "")));
		} else if (mibVal.startsWith("Gauge32:")) {
	    	return getGauge32(Long.valueOf(mibVal.substring("Gauge32:".length()).trim()));
		} else if (mibVal.startsWith("Counter32:")) {
	    	return getCounter32(Long.valueOf(mibVal.substring("Counter32:".length()).trim()));
		} else if (mibVal.startsWith("Counter64:")) {
	    	return getCounter64(BigInteger.valueOf(Long.valueOf(mibVal.substring("Counter64:".length()).trim())));
		} else if (mibVal.startsWith("IpAddress:")) {
	    	return getIpAddress(InetAddrUtils.addr(mibVal.substring("IpAddress:".length()).trim()));
		} else if (mibVal.startsWith("Hex-STRING:")) {
			final String trimmed = mibVal.substring("Hex-STRING:".length()).trim();
			final ByteBuffer bb = ByteBuffer.allocate(trimmed.length());
			if (trimmed.matches("^.*[ :].*$")) {
				for (final String chunk : trimmed.split("[ :]")) {
					short s = Short.valueOf(chunk, 16);
					bb.put((byte)(s & 0xFF));
				}
			} else {
				if (trimmed.length() % 2 != 0) {
					LogUtils.warnf(SnmpUtils.class, "Hex-STRING %s does not have ' ' or ':' separators, but it is an uneven number of characters.", trimmed);
				}
				final Matcher m = MockSnmpValueFactory.HEX_CHUNK_PATTERN.matcher(trimmed);
				while (m.find()) {
	                short s = Short.valueOf(m.group(1), 16);
	                bb.put((byte)(s & 0xFF));
				}
			}
			final byte[] parsed = new byte[bb.position()];
			bb.flip();
			bb.get(parsed);
			return getOctetString(parsed);
	    } else if (mibVal.startsWith("Network Address:")) {
			return getOctetString(mibVal.substring("Network Address:".length()).trim().getBytes());
	    } else if (mibVal.startsWith("BITS:")) {
	        return getOctetString(mibVal.substring("BITS:".length()).trim().getBytes());
	    } else if (mibVal.equals("\"\"")) {
	    	return getNull();
	    }
	
	    throw new IllegalArgumentException("Unknown Snmp Type: "+mibVal);
	}



}
