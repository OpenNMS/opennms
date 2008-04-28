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
// Modifications:
//
// 2008 May 04: Add toHexString for StringSnmpValue. - dj@opennms.org
// 2008 Mar 16: Add Integer32, Gauge32, Counter32, and Counter64 objects
//              and make StringSnmpValue.toLong try to parse the value. - dj@opennms.org
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
package org.opennms.netmgt.snmp.mock;

import java.math.BigInteger;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

public class TestSnmpValue implements SnmpValue {
    
    public static class NetworkAddressSnmpValue extends TestSnmpValue {

        public NetworkAddressSnmpValue(String value) {
            super(SnmpValue.SNMP_OCTET_STRING, value);
        }

        public boolean isDisplayable() {
            return false;
        }

    }

    public static class HexStringSnmpValue extends TestSnmpValue {

        public HexStringSnmpValue(String value) {
            super(SnmpValue.SNMP_OCTET_STRING, value);
        }

        public String toHexString() {
            return toString();
        }
        
        public boolean isDisplayable() {
            return false;
        }

    }

    public static class IpAddressSnmpValue extends TestSnmpValue {

        public IpAddressSnmpValue(String value) {
            super(SnmpValue.SNMP_IPADDRESS, value);
        }

        public InetAddress toInetAddress() {
            try {
                return InetAddress.getByName(toString());
            } catch (Exception e) {
                return super.toInetAddress();
            }
        }
        
        public byte[] getBytes() {
            return toInetAddress().getAddress();
        }
        
        public boolean isDisplayable() {
            return true;
        }

    }

    public static class NumberSnmpValue extends TestSnmpValue {

        public NumberSnmpValue(int type, String value) {
            super(type, value);
        }
        
        public boolean isNumeric() {
            return true;
        }
        
        public int toInt() {
            return Integer.parseInt(getNumberString());
        }
        
        public long toLong() {
            return Long.parseLong(getNumberString());
        }
        
        public BigInteger toBigInteger() {
            return new BigInteger(getNumberString());
        }
        
        public String getNumberString() {
            return toString();
        }
        
        public byte[] getBytes() {
            return toBigInteger().toByteArray();
        }

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
        public Gauge32SnmpValue(int value) {
            this(Integer.toString(value));
        }
        public Gauge32SnmpValue(String value) {
            super(SnmpValue.SNMP_GAUGE32, value);
        }
    }
   
    public static class Counter32SnmpValue extends NumberSnmpValue {
        public Counter32SnmpValue(int value) {
            this(Integer.toString(value));
        }
        public Counter32SnmpValue(String value) {
            super(SnmpValue.SNMP_COUNTER32, value);
        }
    }
    
    public static class Counter64SnmpValue extends NumberSnmpValue {
        public Counter64SnmpValue(long value) {
            this(Long.toString(value));
        }
        public Counter64SnmpValue(String value) {
            super(SnmpValue.SNMP_COUNTER64, value);
        }
    }
    
    public static class TimeticksSnmpValue extends NumberSnmpValue {

        // Format of string is '(numTicks) HH:mm:ss.hh'
        public TimeticksSnmpValue(String value) {
            super(SnmpValue.SNMP_TIMETICKS, value);
        }

        public String getNumberString() {
            String str = toString();
            int end = str.indexOf(')');
            return str.substring(1, end);
        }

    }



    public static class StringSnmpValue extends TestSnmpValue {
        public StringSnmpValue(String value) {
            super(SnmpValue.SNMP_OCTET_STRING, value);
        }
        
        public int toInt() {
            try {
                return Integer.parseInt(toString());
            } catch (NumberFormatException e) {
                return super.toInt();
            }
            
        }

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

    public static class OidSnmpValue extends TestSnmpValue {

        public OidSnmpValue(String value) {
            super(SnmpValue.SNMP_OBJECT_IDENTIFIER, value);
        }

        public SnmpObjId toSnmpObjId() {
            return SnmpObjId.get(toString());
        }

        public boolean isDisplayable() {
            return true;
        }


    }

    private int m_type;
    private String m_value;
    public static final SnmpValue NULL_VALUE = new TestSnmpValue(SnmpValue.SNMP_NULL, null) {
        public boolean isNull() {
            return true;
        }
    };
    public static final SnmpValue NO_SUCH_INSTANCE = new TestSnmpValue(SnmpValue.SNMP_NO_SUCH_INSTANCE, "noSuchInstance");
    public static final SnmpValue NO_SUCH_OBJECT = new TestSnmpValue(SnmpValue.SNMP_NO_SUCH_OBJECT, "noSuchObject");
    public static final SnmpValue END_OF_MIB = new TestSnmpValue(SnmpValue.SNMP_END_OF_MIB, "endOfMibView");
    
    public TestSnmpValue(int type, String value) {
        m_type = type;
        m_value = value;
    }

    public boolean isEndOfMib() {
        return getType() == SnmpValue.SNMP_END_OF_MIB;
    }
    
    public int getType() {
        return m_type;
    }
    
    public String toDisplayString() { return toString(); }
    
    public String toString() { return m_value; }

    public boolean equals(Object obj) {
        if (obj instanceof TestSnmpValue ) {
            TestSnmpValue val = (TestSnmpValue)obj;
            return (m_value == null ? val.m_value == null : m_value.equals(val.m_value));
        }
        return false;
    }

    public int hashCode() {
        if (m_value == null) return 0;
        return m_value.hashCode();
    }

    public boolean isNumeric() {
        return false;
    }
    
    public boolean isError() {
        switch (getType()) {
        case SnmpValue.SNMP_NO_SUCH_INSTANCE:
        case SnmpValue.SNMP_NO_SUCH_OBJECT:
            return true;
        default:
            return false;
        }
        
    }

    public static SnmpValue parseMibValue(String mibVal) {
        if (mibVal.startsWith("OID:"))
            return new OidSnmpValue(mibVal.substring("OID:".length()).trim());
        else if (mibVal.startsWith("Timeticks:"))
            return new TimeticksSnmpValue(mibVal.substring("Timeticks:".length()).trim());
        else if (mibVal.startsWith("STRING:"))
            return new StringSnmpValue(mibVal.substring("STRING:".length()).trim());
        else if (mibVal.startsWith("INTEGER:"))
            return new NumberSnmpValue(SnmpValue.SNMP_INT32, mibVal.substring("INTEGER:".length()).trim());
        else if (mibVal.startsWith("Gauge32:"))
            return new NumberSnmpValue(SnmpValue.SNMP_GAUGE32, mibVal.substring("Gauge32:".length()).trim());
        else if (mibVal.startsWith("Counter32:"))
            return new NumberSnmpValue(SnmpValue.SNMP_COUNTER32, mibVal.substring("Counter32:".length()).trim());
        else if (mibVal.startsWith("Counter64:"))
            return new NumberSnmpValue(SnmpValue.SNMP_COUNTER64, mibVal.substring("Counter64:".length()).trim());
        else if (mibVal.startsWith("IpAddress:"))
            return new IpAddressSnmpValue(mibVal.substring("IpAddress:".length()).trim());
        else if (mibVal.startsWith("Hex-STRING:"))
            return new HexStringSnmpValue(mibVal.substring("Hex-STRING:".length()).trim());
        else if (mibVal.startsWith("Network Address:"))
            return new NetworkAddressSnmpValue(mibVal.substring("Network Address:".length()).trim());
        else if (mibVal.equals("\"\""))
            return NULL_VALUE;

        throw new IllegalArgumentException("Unknown Snmp Type: "+mibVal);
    }

    public int toInt() {
        throw new IllegalArgumentException("Unable to convert "+this+" to an int");
    }

    public InetAddress toInetAddress() {
        throw new IllegalArgumentException("Unable to convert "+this+" to an InetAddress");
    }

    public long toLong() {
        throw new IllegalArgumentException("Unable to convert "+this+" to a long");
    }

    public String toHexString() {
        throw new IllegalArgumentException("Unable to convert "+this+" to a hex string");
    }

    public BigInteger toBigInteger() {
        throw new IllegalArgumentException("Unable to convert "+this+" to a hex string");
    }

    public SnmpObjId toSnmpObjId() {
        throw new IllegalArgumentException("Unable to convert "+this+" to an SnmpObjId");
    }

    public byte[] getBytes() {
        return toString().getBytes();
    }

    public boolean isDisplayable() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

 
}
