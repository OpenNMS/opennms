//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
// 
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//
// SnmpNull.java,v 1.1.1.1 2001/11/11 17:27:22 ben Exp
//
//

//
// Log
//
//	5/15/00 - Weave
//		Added the toString() method.
//

package org.opennms.protocols.snmp;

import java.io.Serializable;

import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * Implements the SNMP Null object as defined by the SNMPv1 and SNMPv2 SMI. The
 * object has no data or length, but is encoded with a specific header. Often
 * used as the value portion of an SnmpVarBind when sending a GET, GETNEXT, etc
 * to a remote agent.
 * 
 * This class manages no internal data, but is derived to provide the proper
 * encoding and handling of SNMP Null datatypes.
 * 
 * @version 1.1.1.1
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public class SnmpNull extends Object implements SnmpSyntax, Cloneable, Serializable {
    /**
     * Used to allow the serialization format to evolve.
     * 
     */
    static final long serialVersionUID = 441279481529521581L;

    /**
     * Defines the ASN.1 value for the SnmpNull class.
     * 
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_NULL;

    /**
     * Constructs a default SnmpNull class
     * 
     */
    public SnmpNull() {
        // do nothing
    }

    /**
     * Copy constructor. Provided so that if the class is extended or should at
     * some point manage internal data, the data could be meaningfully copied
     * from the passed object.
     * 
     * @param second
     *            The class object to set data from.
     * 
     */
    public SnmpNull(SnmpNull second) {
        // do nothing
    }

    /**
     * Used to retreive the ASN.1 type for this object.
     * 
     * @return The ASN.1 value for the SnmpNull
     * 
     */
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Used to encode the null value into an ASN.1 buffer. The passed encoder
     * defines the method for encoding the data.
     * 
     * @param buf
     *            The location to write the encoded data
     * @param offset
     *            The start of the encoded buffer.
     * @param encoder
     *            The ASN.1 encoder object
     * 
     * @return The byte immediantly after the last encoded byte.
     * 
     */
    public int encodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnEncodingException {
        return encoder.buildNull(buf, offset, typeId());
    }

    /**
     * Used to decode the null value from the ASN.1 buffer. The passed encoder
     * is used to decode the ASN.1 information.
     * 
     * @param buf
     *            The encoded ASN.1 data
     * @param offset
     *            The offset of the first byte of data
     * @param encoder
     *            The ASN.1 decoder object.
     * 
     * @return The byte immediantly after the last decoded byte of information.
     * 
     */
    public int decodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnDecodingException {
        Object[] rVals = encoder.parseNull(buf, offset);

        if (((Byte) rVals[1]).byteValue() != typeId())
            throw new AsnDecodingException("Invalid ASN.1 type");

        return ((Integer) rVals[0]).intValue();
    }

    /**
     * Used to get a duplicate of the current object so that it can be modified
     * without affecting the creating object.
     * 
     * @return A duplicate of the current object.
     * 
     */
    public SnmpSyntax duplicate() {
        return new SnmpNull(this);
    }

    /**
     * Used to get a duplicate of the current object so that it can be modified
     * without affecting the creating object.
     * 
     * @return A duplicate of the current object.
     * 
     */
    public Object clone() {
        return new SnmpNull(this);
    }

    /**
     * Returns a string representation of the SNMP Null object
     * 
     */
    public String toString() {
        return "";
    }
}
