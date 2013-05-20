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

package org.opennms.protocols.snmp;

/**
 * The SnmpOpaque class is an extension of the octet string class and is used to
 * pass opaque data. Opaque data is information that isn't interperted by the
 * manager in general.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
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
     * 
     */
    public SnmpOpaque() {
        super();
    }

    /**
     * Constructs an opaque object with the passed data.
     * 
     * @param data
     *            The opaque data.
     * 
     */
    public SnmpOpaque(byte[] data) {
        super(data);
    }

    /**
     * Constructs an object that is a duplicate of the passed object.
     * 
     * @param second
     *            The object to be duplicated.
     * 
     */
    public SnmpOpaque(SnmpOpaque second) {
        super(second);
    }

    /**
     * Constructs an object that is a duplicate of the passed object.
     * 
     * @param second
     *            The object to be duplicated.
     * 
     */
    public SnmpOpaque(SnmpOctetString second) {
        super(second);
    }

    /**
     * Returns the defined ASN.1 type identifier.
     * 
     * @return The ASN.1 identifier.
     * 
     */
    @Override
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Returns a duplicate of the current object.
     * 
     * @return A duplicate of self
     * 
     */
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpOpaque(this);
    }

    /**
     * Returns a duplicate of the current object.
     * 
     * @return A duplicate of self
     * 
     */
    @Override
    public Object clone() {
        return new SnmpOpaque(this);
    }

    /**
     * Returns a string representation of the object.
     * 
     */
    @Override
    public String toString() {
        //
        // format the string for hex
        //
    	final byte[] data = getString();
    	final StringBuffer b = new StringBuffer();
        // b.append("SNMP Opaque [length = " + data.length + ", fmt = HEX] =
        // [");
        for (int i = 0; i < data.length; ++i) {
            int x = (int) data[i] & 0xff;
            if (x < 16)
                b.append('0');
            b.append(Integer.toString(x, 16).toUpperCase());

            if (i + 1 < data.length)
                b.append(' ');
        }
        // b.append(']');
        return b.toString();
    }
}
