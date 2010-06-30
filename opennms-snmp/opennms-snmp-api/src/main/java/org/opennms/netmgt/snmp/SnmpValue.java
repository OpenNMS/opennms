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
 * <p>SnmpValue interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface SnmpValue {
    // These values match the ASN.1 constants
    /** Constant <code>SNMP_INT32=(0x02)</code> */
    public final static int SNMP_INT32 = (0x02);

    /** Constant <code>SNMP_OCTET_STRING=(0x04)</code> */
    public final static int SNMP_OCTET_STRING = (0x04);

    /** Constant <code>SNMP_NULL=(0x05)</code> */
    public final static int SNMP_NULL = (0x05);

    /** Constant <code>SNMP_OBJECT_IDENTIFIER=(0x06)</code> */
    public final static int SNMP_OBJECT_IDENTIFIER = (0x06);

    /** Constant <code>SNMP_IPADDRESS=(0x40)</code> */
    public final static int SNMP_IPADDRESS = (0x40);

    /** Constant <code>SNMP_COUNTER32=(0x41)</code> */
    public final static int SNMP_COUNTER32 = (0x41);

    /** Constant <code>SNMP_GAUGE32=(0x42)</code> */
    public final static int SNMP_GAUGE32 = (0x42);

    /** Constant <code>SNMP_TIMETICKS=(0x43)</code> */
    public final static int SNMP_TIMETICKS = (0x43);

    /** Constant <code>SNMP_OPAQUE=(0x44)</code> */
    public final static int SNMP_OPAQUE = (0x44);

    /** Constant <code>SNMP_COUNTER64=(0x46)</code> */
    public final static int SNMP_COUNTER64 = (0x46);
    
    /** Constant <code>SNMP_NO_SUCH_OBJECT=(0x80)</code> */
    public final static int SNMP_NO_SUCH_OBJECT = (0x80);
    
    /** Constant <code>SNMP_NO_SUCH_INSTANCE=(0x81)</code> */
    public final static int SNMP_NO_SUCH_INSTANCE = (0x81);

    /** Constant <code>SNMP_END_OF_MIB=(0x82)</code> */
    public final static int SNMP_END_OF_MIB = (0x82);
    
    /**
     * <p>isEndOfMib</p>
     *
     * @return a boolean.
     */
    public abstract boolean isEndOfMib();
    
    /**
     * <p>isError</p>
     *
     * @return a boolean.
     */
    public abstract boolean isError();

    /**
     * <p>isNull</p>
     *
     * @return a boolean.
     */
    public abstract boolean isNull();

    /**
     * <p>isDisplayable</p>
     *
     * @return a boolean.
     */
    public abstract boolean isDisplayable();

    /**
     * <p>isNumeric</p>
     *
     * @return a boolean.
     */
    public abstract boolean isNumeric();

    /**
     * <p>toInt</p>
     *
     * @return a int.
     */
    public abstract int toInt();

    /**
     * <p>toDisplayString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String toDisplayString();

    /**
     * <p>toInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public abstract InetAddress toInetAddress();

    /**
     * <p>toLong</p>
     *
     * @return a long.
     */
    public abstract long toLong();
    
    /**
     * <p>toBigInteger</p>
     *
     * @return a {@link java.math.BigInteger} object.
     */
    public abstract BigInteger toBigInteger();

    /**
     * <p>toHexString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String toHexString();
    
    /**
     * <p>getType</p>
     *
     * @return a int.
     */
    public abstract int getType();
    
    /**
     * <p>getBytes</p>
     *
     * @return an array of byte.
     */
    public abstract byte[] getBytes();

    /**
     * <p>toSnmpObjId</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public abstract SnmpObjId toSnmpObjId();


}
