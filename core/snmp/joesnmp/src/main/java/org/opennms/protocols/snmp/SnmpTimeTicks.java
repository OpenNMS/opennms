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
 * Defines a SNMPv1 32-bit time ticks object. The object is a 32-bit unsigned
 * value that is incremented periodically by an agent using a specific timer
 * interval.
 * 
 * The object inherients and uses most of the methods defined by the SnmpUInt32
 * class. This class does not define any specific data, but is instead used to
 * override the ASN.1 type of the base class.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public class SnmpTimeTicks extends SnmpUInt32 {
    /**
     * Defines the serialization format
     */
    static final long serialVersionUID = 5452427494814505218L;

    /**
     * Defines the ASN.1 type for this object.
     * 
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_TIMETICKS;

    /**
     * Constructs the default counter object. The initial value is defined by
     * the super class default constructor
     */
    public SnmpTimeTicks() {
        super();
    }

    /**
     * Constructs the object with the specified value.
     * 
     * @param value
     *            The default value for the object.
     * 
     */
    public SnmpTimeTicks(long value) {
        super(value);
    }

    /**
     * Constructs the object with the specified value.
     * 
     * @param value
     *            The default value for the object.
     * 
     */
    public SnmpTimeTicks(Long value) {
        super(value);
    }

    /**
     * Constructs a new object with the same value as the passed object.
     * 
     * @param second
     *            The object to recover values from.
     * 
     */
    public SnmpTimeTicks(SnmpTimeTicks second) {
        super(second);
    }

    /**
     * Constructs a new object with the value constained in the SnmpUInt32
     * object.
     * 
     * @param uint32
     *            The SnmpUInt32 object to copy.
     * 
     */
    public SnmpTimeTicks(SnmpUInt32 uint32) {
        super(uint32);
    }

    /**
     * <p>
     * Simple class constructor that is used to create an initialize the new
     * instance with the unsigned value decoded from the passed String argument.
     * If the decoded argument is malformed, null, or evaluates to a negative
     * value then an exception is generated.
     * </p>
     * 
     * <p>
     * The value passed to the constructor should be the number of milliseconds
     * since epoch, as defined by RFC 1155.
     * </p>
     * 
     * @param value
     *            The string encoded value of TimeTicks
     * 
     * @throws java.lang.NumberFormatException
     *             Thrown if the passed value is malformed and cannot be parsed.
     * @throws java.lang.IllegalArgumentException
     *             Throws if the passed value evaluates to a negative value.
     * @throws java.lang.NullPointerException
     *             Throws if the passed value is a null reference.
     */
    public SnmpTimeTicks(String value) {
        super(value);
    }

    /**
     * Returns the ASN.1 type specific to this object.
     * 
     * @return The ASN.1 value for this object.
     */
    @Override
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Creates a new object that is a duplicate of the current object.
     * 
     * @return The newly created duplicate object.
     * 
     */
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpTimeTicks(this);
    }

    /**
     * Creates a new object that is a duplicate of the current object.
     * 
     * @return The newly created duplicate object.
     * 
     */
    @Override
    public Object clone() {
        return new SnmpTimeTicks(this);
    }

    /**
     * Returns the string representation of the object.
     * 
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        long time = getValue();
        long tmp = 0;
        if ((tmp = (time / (24 * 3600 * 100))) > 0) {
            buf.append(tmp).append("d ");
            time = time % (24 * 3600 * 100);
        } else
            buf.append("0d ");

        if ((tmp = time / (3600 * 100)) > 0) {
            buf.append(tmp).append("h ");
            time = time % (3600 * 100);
        } else
            buf.append("0h ");

        if ((tmp = time / 6000) > 0) {
            buf.append(tmp).append("m ");
            time = time % 6000;
        } else
            buf.append("0m ");

        if ((tmp = time / 100) > 0) {
            buf.append(tmp).append("s ");
            time = time % 100;
        } else
            buf.append("0s ");

        buf.append(time * 10).append("ms");

        return buf.toString();
    }

}
