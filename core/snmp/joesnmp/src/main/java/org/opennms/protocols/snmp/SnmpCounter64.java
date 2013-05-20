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

import java.io.Serializable;
import java.math.BigInteger;

import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * This class defines the 64-bit SNMP counter object used to transmit 64-bit
 * unsigned number.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public class SnmpCounter64 extends Object implements SnmpSyntax, Cloneable, Serializable {
    /**
     * defines the serialization version
     */
    static final long serialVersionUID = -192572343143669856L;

    /**
     * The internal 64-bit unsigned quantity implemented as a 64-bit signed
     * quantity
     * 
     */
    private BigInteger m_value;

    /**
     * The ASN.1 value for an unsigned integer value. BEWARE this value will
     * conflict with the SnmpSMI.SMI_COUNTER32 value. This object should not be
     * dynamically registered with the SNMP library
     * 
     */
    public static final byte ASNTYPE = SnmpSMI.SMI_COUNTER64;

    /**
     * Default class constructor. Constructs the object with a value of zero(0).
     * 
     */
    public SnmpCounter64() {
        m_value = BigInteger.valueOf(0L);
        m_value.clearBit(65); // ensures that the 64-bits are treated as
                                // unsigned!
    }

    /**
     * Constructs a SnmpCounter64 object with the specified value.
     * 
     * @param value
     *            The new 64-bit value.
     * 
     */
    public SnmpCounter64(long value) {
        m_value = BigInteger.valueOf(value & Long.MAX_VALUE);
        m_value.clearBit(65); // ensures that the 64-bits are treated as
                                // unsigned!
    }

    /**
     * Constructs a SnmpCounter64 object with the specified value.
     * 
     * @param value
     *            The new 64-bit value.
     * 
     */
    public SnmpCounter64(BigInteger value) {
        m_value = new BigInteger(value.toByteArray());
        m_value.clearBit(65); // ensures that the 64-bits are treated as
                                // unsigned!
    }

    /**
     * Class copy constructor. Constructs a new object with the same value as
     * the passed object.
     * 
     * @param second
     *            The object to copy the value from.
     * 
     */
    public SnmpCounter64(SnmpCounter64 second) {
        m_value = new BigInteger(second.m_value.toByteArray());
        m_value.clearBit(65); // ensures that the 64-bits are treated as
                                // unsigned!
    }

    /**
     * <p>
     * Simple class constructor that is used to create an initialize the new
     * instance with the unsigned value decoded from the passed String argument.
     * If the decoded argument is malformed, null, or evaluates to a negative
     * value then an exception is generated.
     * </p>
     * 
     * @param value
     *            The string encoded value.
     * 
     * @throws java.lang.NumberFormatException
     *             Thrown if the passed value is malformed and cannot be parsed.
     * @throws java.lang.IllegalArgumentException
     *             Throws if the passed value evaluates to a negative value.
     * @throws java.lang.NullPointerException
     *             Throws if the passed value is a null reference.
     */
    public SnmpCounter64(String value) {
        if (value == null)
            throw new NullPointerException("The constructor argument must not be null");

        m_value = new BigInteger(value);
        if (m_value.signum() < 0)
            throw new IllegalArgumentException("The decoded value may not be negative");
    }

    /**
     * Used to retreive the 64-bit unsigned value.
     * 
     * @return The internal 64-bit value.
     * 
     */
    public BigInteger getValue() {
        return m_value;
    }

    /**
     * Used to set the 64-bit unsigned quantity. If the value exceeds 64-bit
     * then the upper 64-bits will be silently truncated from the value.
     * 
     * @param value
     *            The new value for the object
     */
    public void setValue(BigInteger value) {
        m_value = new BigInteger(value.toByteArray());
        m_value.clearBit(65); // ensure 64-bit unsigned comparisions!
    }

    /**
     * Used to retreive the ASN.1 type for this object.
     * 
     * @return The ASN.1 value for the SnmpCounter64
     * 
     */
    @Override
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Used to encode the integer value into an ASN.1 buffer. The passed encoder
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
    @Override
    public int encodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnEncodingException {
        return encoder.buildUInteger64(buf, offset, typeId(), getValue());
    }

    /**
     * Used to decode the integer value from the ASN.1 buffer. The passed
     * encoder is used to decode the ASN.1 information and the integer value is
     * stored in the internal object.
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
    @Override
    public int decodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnDecodingException {
        Object[] rVals = encoder.parseUInteger64(buf, offset);

        if (((Byte) rVals[1]).byteValue() != typeId())
            throw new AsnDecodingException("Invalid ASN.1 type");

        setValue((BigInteger) rVals[2]);

        return ((Integer) rVals[0]).intValue();
    }

    /**
     * Returns a duplicte of the current object
     * 
     * @return A duplciate copy of the current object
     */
    @Override
    public SnmpSyntax duplicate() {
        return new SnmpCounter64(this);
    }

    /**
     * Returns a duplicte of the current object
     * 
     * @return A duplciate copy of the current object
     */
    @Override
    public Object clone() {
        return new SnmpCounter64(this);
    }

    /**
     * Returns the string representation of the object.
     * 
     */
    @Override
    public String toString() {
        return getValue().toString();
    }
}
