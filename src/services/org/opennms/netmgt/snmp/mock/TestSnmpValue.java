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
package org.opennms.netmgt.snmp.mock;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpValue;

public class TestSnmpValue implements SnmpValue {
    
    public static class NetworkAddressSnmpValue extends TestSnmpValue {

        public NetworkAddressSnmpValue(String value) {
            super(value);
        }

    }

    public static class HexStringSnmpValue extends TestSnmpValue {

        public HexStringSnmpValue(String value) {
            super(value);
        }

        public String toHexString() {
            return toString();
        }
        
    }

    public static class IpAddressSnmpValue extends TestSnmpValue {

        public IpAddressSnmpValue(String value) {
            super(value);
        }

        public InetAddress toInetAddress() {
            try {
                return InetAddress.getByName(toString());
            } catch (Exception e) {
                return super.toInetAddress();
            }
        }
        
    }

    public static class NumberSnmpValue extends TestSnmpValue {

        public NumberSnmpValue(String value) {
            super(value);
        }
        
        public boolean isNumeric() {
            return true;
        }
        
        public int toInt() {
            return Integer.parseInt(toString());
        }
        
        public long toLong() {
            return Long.parseLong(toString());
        }

    }

    public static class StringSnmpValue extends TestSnmpValue {

        public StringSnmpValue(String value) {
            super(value);
        }
        
        public int toInt() {
            try {
                return Integer.parseInt(toString());
            } catch (NumberFormatException e) {
                return super.toInt();
            }
            
        }

    }

    public static class TimeticksSnmpValue extends TestSnmpValue {

        // Format of string is '(numTicks) HH:mm:ss.hh'
        public TimeticksSnmpValue(String value) {
            super(value);
        }

        public boolean isNumeric() {
            return true;
        }
        
        public int toInt() {
            String str = toString();
            int end = str.indexOf(')');
            return Integer.parseInt(toString().substring(1, end));
        }

    }

    public static class OidSnmpValue extends TestSnmpValue {

        public OidSnmpValue(String value) {
            super(value);
        }

    }


    String m_value;
    public static final SnmpValue NULL_VALUE = new TestSnmpValue(null);
    public static final SnmpValue NO_SUCH_INSTANCE = new TestSnmpValue("noSuchInstance");
    public static final SnmpValue NO_SUCH_OBJECT = new TestSnmpValue("noSuchObject");
    public static final SnmpValue END_OF_MIB = new TestSnmpValue("endOfMibView") {
        public boolean isEndOfMib() { return true; }
    };
    
    public TestSnmpValue(String value) {
        m_value = value;
    }

    public boolean isEndOfMib() {
        return false;
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

    static SnmpValue parseMibValue(String mibVal) {
        if (mibVal.startsWith("OID:"))
            return new OidSnmpValue(mibVal.substring("OID:".length()).trim());
        else if (mibVal.startsWith("Timeticks:"))
            return new TimeticksSnmpValue(mibVal.substring("Timeticks:".length()).trim());
        else if (mibVal.startsWith("STRING:"))
            return new StringSnmpValue(mibVal.substring("STRING:".length()).trim());
        else if (mibVal.startsWith("INTEGER:"))
            return new NumberSnmpValue(mibVal.substring("INTEGER:".length()).trim());
        else if (mibVal.startsWith("Gauge32:"))
            return new NumberSnmpValue(mibVal.substring("Gauge32:".length()).trim());
        else if (mibVal.startsWith("Counter32:"))
            return new NumberSnmpValue(mibVal.substring("Counter32:".length()).trim());
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
        throw new IllegalArgumentException("Unable to convert "+this+" to an IpAddress");
    }

    public long toLong() {
        throw new IllegalArgumentException("Unable to convert "+this+" to a long");
    }

    public String toHexString() {
        throw new IllegalArgumentException("Unable to convert "+this+" to a hex string");
    }
    
}