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
// SnmpOpaque.java,v 1.1.1.1 2001/11/11 17:27:22 ben Exp
//
//

//
// Log:
//
//	5/15/00 - Weave
//		Added the toString() method
//

package org.opennms.protocols.snmp;

/**
 * The SnmpOpaque class is an extension of the octet string class and is used to
 * pass opaque data. Opaque data is information that isn't interperted by the
 * manager in general.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version 1.1.1.1
 */
public class SnmpOpaque extends SnmpOctetString {
    /**
     * Required for version control of serialzation format.
     */
    static final long serialVersionUID = -6031084829130590165L;

    /**
     * The ASN.1 type for this class.
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_OPAQUE;

    /**
     * The default constructor for this class.
     */
    public SnmpOpaque() {
        super();
    }

    /**
     * Constructs an opaque object with the passed data.
     *
     * @param data
     *            The opaque data.
     */
    public SnmpOpaque(byte[] data) {
        super(data);
    }

    /**
     * Constructs an object that is a duplicate of the passed object.
     *
     * @param second
     *            The object to be duplicated.
     */
    public SnmpOpaque(SnmpOpaque second) {
        super(second);
    }

    /**
     * Constructs an object that is a duplicate of the passed object.
     *
     * @param second
     *            The object to be duplicated.
     */
    public SnmpOpaque(SnmpOctetString second) {
        super(second);
    }

    /**
     * Returns the defined ASN.1 type identifier.
     *
     * @return The ASN.1 identifier.
     */
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Returns a duplicate of the current object.
     *
     * @return A duplicate of self
     */
    public SnmpSyntax duplicate() {
        return new SnmpOpaque(this);
    }

    /**
     * Returns a duplicate of the current object.
     *
     * @return A duplicate of self
     */
    public Object clone() {
        return new SnmpOpaque(this);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        //
        // format the string for hex
        //
        byte[] data = getString();
        StringBuffer b = new StringBuffer();
        // b.append("SNMP Opaque [length = " + data.length + ", fmt = HEX] =
        // [");
        for (int i = 0; i < data.length; ++i) {
            int x = (int) data[i] & 0xff;
            if (x < 16)
                b.append('0');
            b.append(Integer.toString(x, 16).toUpperCase());

            if (i < data.length)
                b.append(' ');
        }
        // b.append(']');
        return b.toString();
    }
}
