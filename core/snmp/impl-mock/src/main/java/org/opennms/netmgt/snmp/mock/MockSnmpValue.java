/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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
import java.net.UnknownHostException;

import org.opennms.netmgt.snmp.AbstractSnmpValue;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

public class MockSnmpValue extends AbstractSnmpValue {
	private static final class MockSnmpNullValue extends MockSnmpValue {
		private MockSnmpNullValue(int type, String value) {
			super(type, value);
		}

                @Override
		public boolean isNull() {
            return true;
        }
	}

	public static class NetworkAddressSnmpValue extends MockSnmpValue {

        public NetworkAddressSnmpValue(final String value) {
            super(SnmpValue.SNMP_OCTET_STRING, value);
        }

        @Override
        public boolean isDisplayable() {
            return false;
        }

    }
    
    public static class OctetStringSnmpValue extends MockSnmpValue {
		private byte[] m_bytes;

    	public OctetStringSnmpValue(final byte[] bytes) {
    		super(SnmpValue.SNMP_OCTET_STRING, new String(bytes));
    		m_bytes = bytes;
    	}

                @Override
        public byte[] getBytes() {
        	return m_bytes;
        }
        
                @Override
        public String toString() {
            final byte[] data = getBytes();
            
            final byte[] results = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                results[i] = Character.isISOControl((char)data[i]) ? (byte)'.' : data[i];
            }
            return new String(results);
        }

                @Override
        public String toHexString() {
        	final byte[] data = getBytes();
            final StringBuffer b = new StringBuffer();
            for (int i = 0; i < data.length; ++i) {
                final int x = (int) data[i] & 0xff;
                if (x < 16) b.append("0");
                b.append(Integer.toString(x, 16).toLowerCase());
            }
            return b.toString();
        }
        
                @Override
        public boolean isDisplayable() {
            return allBytesDisplayable(getBytes());
        }

    }

    public static class IpAddressSnmpValue extends MockSnmpValue {
    	
    	public IpAddressSnmpValue(InetAddress addr) {
    		this(InetAddrUtils.str(addr));
    	}
    	
    	public IpAddressSnmpValue(byte[] bytes) {
    		this(addrStr(bytes));
    	}
    	
    	public static String addrStr(byte[] bytes) {
    		try {
				return InetAddrUtils.str(InetAddress.getByAddress(bytes));
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
    	}

        public IpAddressSnmpValue(String value) {
            super(SnmpValue.SNMP_IPADDRESS, value);
        }

            @Override
        public InetAddress toInetAddress() {
            try {
                return InetAddrUtils.addr(toString());
            } catch (Exception e) {
                return super.toInetAddress();
            }
        }
        
            @Override
        public byte[] getBytes() {
            return toInetAddress().getAddress();
        }
        
            @Override
        public boolean isDisplayable() {
            return true;
        }

    }

    public static class NumberSnmpValue extends MockSnmpValue {

        public NumberSnmpValue(int type, String value) {
            super(type, value);
        }
        
        @Override
        public boolean isNumeric() {
            return true;
        }
        
        @Override
        public int toInt() {
            return Integer.parseInt(getNumberString());
        }
        
        @Override
        public long toLong() {
            return Long.parseLong(getNumberString());
        }
        
        @Override
        public BigInteger toBigInteger() {
            return new BigInteger(getNumberString());
        }
        
        public String getNumberString() {
            return toString();
        }
        
        @Override
        public byte[] getBytes() {
            return toBigInteger().toByteArray();
        }

        @Override
        public boolean isDisplayable() {
            return true;
        }
    }
    
    public static class Integer32SnmpValue extends NumberSnmpValue {
        public Integer32SnmpValue(int value) {
            this(Integer.toString(value));
        }
        public Integer32SnmpValue(String value) {
            super(SnmpValue.SNMP_INT32, value);
        }
    }
    
    public static class Gauge32SnmpValue extends NumberSnmpValue {
        public Gauge32SnmpValue(long value) {
            this(Long.toString(value));
        }
        public Gauge32SnmpValue(String value) {
            super(SnmpValue.SNMP_GAUGE32, value);
        }
    }
   
    public static class Counter32SnmpValue extends NumberSnmpValue {
		public Counter32SnmpValue(long value) {
            this(Long.toString(value));
        }
        public Counter32SnmpValue(String value) {
            super(SnmpValue.SNMP_COUNTER32, value);
        }

		@Override
		public BigInteger toBigInteger() {
			return BigInteger.valueOf(toLong());
		}
    }
    
    public static class Counter64SnmpValue extends NumberSnmpValue {
        public Counter64SnmpValue(BigInteger value) {
            this(value.toString());
        }
        public Counter64SnmpValue(String value) {
            super(SnmpValue.SNMP_COUNTER64, value);
        }
    }
    
    static enum UnitOfTime {
        DAYS(4), HOURS(3), MINUTES(2), SECONDS(1), MILLIS(0);
        
        private int m_index;
        
        private UnitOfTime(int index) {
            m_index = index;
        }
        
        private static final long[] s_millisPerUnit = {
            1L,                         // millis
            1000L,                      // seconds
            1000L * 60L,                // minutes
            1000L * 60L * 60L,          // hours
            1000L * 60L * 60L * 24L     // days
        };
        
        private static final String[] s_unitName = {
            "ms",   // millis
            "s",    // seconds
            "m",    // minutes
            "h",    // hours
            "d"     // days
        };
        
        public long wholeUnits(long millis) {
            return millis / s_millisPerUnit[m_index];
        }
        
        public long remainingMillis(long millis) {
            return millis % s_millisPerUnit[m_index];
        }
        
        public String unit() {
            return s_unitName[m_index];
        }
        
        
    }
    
    public static class TimeticksSnmpValue extends NumberSnmpValue {

        // Format of string is '(numTicks) HH:mm:ss.hh'
        public TimeticksSnmpValue(String value) {
            super(SnmpValue.SNMP_TIMETICKS, value);
        }
        
        public TimeticksSnmpValue(long centiSeconds) {
        	this("(" + centiSeconds + ")");
        }

        @Override
        public String getNumberString() {
            String str = getValue();
            int end = str.indexOf(')');
            return (end < 0 ? str : str.substring(1, end));
        }
        
        @Override
        public String toString() {
        	return String.valueOf(toLong());
        }
        
        @Override
        public String toDisplayString() {
        	return toString();
        	/*
            long millis = toLong()*10L;
            
            StringBuilder buf = new StringBuilder();

            boolean first = true;
            for(UnitOfTime unit : UnitOfTime.values()) {

                if (first) {
                    first = false; 
                 } else {
                     buf.append(' ');
                 }

                buf.append(unit.wholeUnits(millis)).append(unit.unit());
                millis = unit.remainingMillis(millis);
            }
            
            return buf.toString();
            */
        }

    }



    public static class StringSnmpValue extends MockSnmpValue {
        public StringSnmpValue(String value) {
            super(SnmpValue.SNMP_OCTET_STRING, value);
        }
        
        @Override
        public int toInt() {
            try {
                return Integer.parseInt(toString());
            } catch (NumberFormatException e) {
                return super.toInt();
            }
            
        }

        @Override
        public boolean isDisplayable() {
            return true;
        }
        
        @Override
        public long toLong() {
            return Long.parseLong(toString());
        }

        @Override
        public String toHexString() {
            StringBuffer buff = new StringBuffer();

            for (byte b : toString().getBytes()) {
                buff.append(Integer.toHexString(b));
            }
            
            return buff.toString();
        }
    }

    public static class OidSnmpValue extends MockSnmpValue {

    	public OidSnmpValue(SnmpObjId objId) {
    		this(objId.toString());
    	}
    	
        public OidSnmpValue(String value) {
            super(SnmpValue.SNMP_OBJECT_IDENTIFIER, value);
        }

            @Override
        public SnmpObjId toSnmpObjId() {
            return SnmpObjId.get(toString());
        }

            @Override
        public boolean isDisplayable() {
            return true;
        }


    }

    private int m_type;
    private String m_value;
    public static final MockSnmpValue NULL_VALUE = new MockSnmpNullValue(SnmpValue.SNMP_NULL, null);
    public static final MockSnmpValue NO_SUCH_INSTANCE = new MockSnmpNullValue(SnmpValue.SNMP_NO_SUCH_INSTANCE, "noSuchInstance");
    public static final MockSnmpValue NO_SUCH_OBJECT = new MockSnmpNullValue(SnmpValue.SNMP_NO_SUCH_OBJECT, "noSuchObject") ;
    public static final MockSnmpValue END_OF_MIB = new MockSnmpNullValue(SnmpValue.SNMP_END_OF_MIB, "endOfMibView");

    public MockSnmpValue(int type, String value) {
        m_type = type;
        m_value = value;
    }

        @Override
    public boolean isEndOfMib() {
        return getType() == SnmpValue.SNMP_END_OF_MIB;
    }
    
        @Override
    public int getType() {
        return m_type;
    }
    
        @Override
    public String toDisplayString() { return toString(); }
    
        @Override
    public String toString() { return m_value; }
    
    public String getValue() { return m_value; }

        @Override
    public boolean equals(Object obj) {
        if (obj instanceof MockSnmpValue ) {
            MockSnmpValue val = (MockSnmpValue)obj;
            return (m_value == null ? val.m_value == null : m_value.equals(val.m_value));
        }
        return false;
    }

        @Override
    public int hashCode() {
        if (m_value == null) return 0;
        return m_value.hashCode();
    }

        @Override
    public boolean isNumeric() {
        return false;
    }
    
        @Override
    public boolean isError() {
        switch (getType()) {
        case SnmpValue.SNMP_NO_SUCH_INSTANCE:
        case SnmpValue.SNMP_NO_SUCH_OBJECT:
            return true;
        default:
            return false;
        }
        
    }

        @Override
    public int toInt() {
        throw new IllegalArgumentException("Unable to convert "+this+" to an int");
    }

        @Override
    public InetAddress toInetAddress() {
        throw new IllegalArgumentException("Unable to convert "+this+" to an InetAddress");
    }

        @Override
    public long toLong() {
        throw new IllegalArgumentException("Unable to convert "+this+" to a long");
    }

        @Override
    public String toHexString() {
        throw new IllegalArgumentException("Unable to convert "+this+" to a hex string");
    }

        @Override
    public BigInteger toBigInteger() {
        throw new IllegalArgumentException("Unable to convert "+this+" to a hex string");
    }

        @Override
    public SnmpObjId toSnmpObjId() {
        throw new IllegalArgumentException("Unable to convert "+this+" to an SNMP object ID");
    }

        @Override
    public byte[] getBytes() {
        return toString().getBytes();
    }

        @Override
    public boolean isDisplayable() {
        return false;
    }

        @Override
    public boolean isNull() {
        return false;
    }

 


}
