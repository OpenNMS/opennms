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

import org.opennms.protocols.snmp.asn1.ASN1;
import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * This class defined the SNMP variables that are transmitted to and from an
 * agent. A variable is defined by its name (a SnmpObjectId) and its value
 * (SnmpSyntax).
 * 
 * The SnmpVarBind is used by the SnmpPduPacket class and uses SnmpObjectId
 * along with any class that implements the SnmpSyntax interface.
 * 
 * @see SnmpSyntax
 * @see SnmpPduPacket
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version 1.1.1.1
 * 
 */
public class SnmpVarBind extends Object implements SnmpSyntax, Cloneable, Serializable {
    /**
     * Allows for evolution of serialization format.
     * 
     */
    static final long serialVersionUID = 2328987288282447623L;

    /**
     * The object identifier that uniquely identifies the "value".
     */
    private SnmpObjectId m_name;

    /**
     * The actual value object associated with the object identifier.
     */
    private SnmpSyntax m_value;

    /**
     * The ASN.1 identifier used to mark SNMP variables
     */
    public static final byte ASNTYPE = (ASN1.SEQUENCE | ASN1.CONSTRUCTOR);

    /**
     * The default class constructor. Constructs an SnmpVarBind with a default
     * object identifier and a null value
     * 
     * @see SnmpObjectId#SnmpObjectId
     * @see SnmpNull#SnmpNull
     */
    public SnmpVarBind() {
        m_name = new SnmpObjectId();
        m_value = new SnmpNull();
    }

    /**
     * Constructs a specific variable with the "name" equal to the passed object
     * identifier. The variable portion is set to an instance of SnmpNull.
     * 
     * @param name
     *            The object identifer name for this variable.
     */
    public SnmpVarBind(SnmpObjectId name) {
        m_name = (SnmpObjectId) name.duplicate();
        m_value = new SnmpNull();
    }

    /**
     * Constructs a variable with the passed name and value. The name and value
     * are duplicated so that any further changes to the passed data will not
     * affect the SNMP variable.
     * 
     * @param name
     *            The object identifier name
     * @param value
     *            The syntax object.
     * 
     */
    public SnmpVarBind(SnmpObjectId name, SnmpSyntax value) {
        m_name = (SnmpObjectId) name.duplicate();
        m_value = value.duplicate();
    }

    /**
     * Constructs a new variable with the give name. The name must be a dotted
     * decimal object identifier string.
     * 
     * @param name
     *            Dotted decimal object identifier.
     * 
     * @see SnmpObjectId
     * 
     */
    public SnmpVarBind(String name) {
        m_name = new SnmpObjectId(name);
        m_value = new SnmpNull();
    }

    /**
     * Constructs a new variable with the passed name and value. The name must
     * be in a dotted decimal format. The value must not be null, but must be a
     * valid SnmpSyntax object.
     * 
     * @param name
     *            The dotted decimal object identifer name
     * @param value
     *            The SnmpSyntax value for the variable
     * 
     */
    public SnmpVarBind(String name, SnmpSyntax value) {
        m_name = new SnmpObjectId(name);
        m_value = value.duplicate();
    }

    /**
     * Class copy constructor. Makes a duplicate copy of the passed variable and
     * stores the new references in self. If the passed variable is modified
     * after the construction, it will not affect the object.
     * 
     * @param second
     *            The variable to copy
     * 
     */
    public SnmpVarBind(SnmpVarBind second) {
        m_name = (SnmpObjectId) second.m_name.duplicate();
        m_value = second.m_value.duplicate();
    }

    /**
     * Returns the object identifier that names the variable.
     * 
     * @return The variable's object identifier
     */
    public SnmpObjectId getName() {
        return m_name;
    }

    /**
     * Sets the variable's object identifier name.
     * 
     * @param name
     *            The new object id for the variable.
     * 
     */
    public void setName(SnmpObjectId name) {
        m_name = (SnmpObjectId) name.duplicate();
    }

    /**
     * Sets the variable's name to passed value.
     * 
     * @param name
     *            The dotted decimal object identifier.
     * 
     */
    public void setName(String name) {
        m_name = new SnmpObjectId(name);
    }

    /**
     * Retreives the variable's value.
     * 
     * @return The SnmpSyntax object for the variable.
     */
    public SnmpSyntax getValue() {
        return m_value;
    }

    /**
     * Sets the value for the variable
     * 
     * @param value
     *            The new value for the object
     * 
     */
    public void setValue(SnmpSyntax value) {
        m_value = value.duplicate();
    }

    /**
     * Returns the ASN.1 type id for the object.
     * 
     * @return The ASN.1 type identifier.
     * 
     */
    @Override
    public byte typeId() {
        return ASNTYPE;
    }

    /**
     * Encodes the SnmpVarBind object into the passed buffer. The variable's
     * data is encoded using the passed AsnEncoder object. The offset for the
     * next object to be encoded is returned by the method. This method is
     * defined to fulfill the contract with the SnmpSytnax interface.
     * 
     * @param buf
     *            Storeage for the encoded data
     * @param offset
     *            Offset to start encoding data
     * @param encoder
     *            The encoder used to convert the data
     * 
     * @exception AnsEncodingException
     *                Thrown if the encoder encounters an error while building
     *                the buffer.
     * 
     * @return The offset of the next byte immediantly after the last encoded
     *         byte by this routine.
     * 
     */
    @Override
    public int encodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnEncodingException {
        int begin = offset;

        //
        // encode the name
        //
        offset = m_name.encodeASN(buf, offset, encoder);
        offset = m_value.encodeASN(buf, offset, encoder);

        int pivot = offset;
        int end = encoder.buildHeader(buf, offset, typeId(), pivot - begin);
        //
        // now rotate!
        //
        SnmpUtil.rotate(buf, begin, pivot, end);

        return end;
    }

    /**
     * Used to recover the encoded variable data from the passed ASN.1 buffer.
     * The encoder object provides a way for the data to be decoded. The offset
     * marks the start location for the decoding operation. Once the data is
     * decoded it is set in the current object.
     * 
     * @param buf
     *            Encoded ASN.1 data
     * @param offset
     *            Offset to first byte of encoded data
     * @param encoder
     *            The encoder used to convert the data
     * 
     * @return The byte offset immediantly after the last decoded byte of
     *         information.
     * 
     * @exception AnsDecodingException
     *                Thrown if an error occurs while attempting to decode the
     *                data. This exception will be thrown byte encoder object.
     * 
     */
    @Override
    public int decodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnDecodingException {
        Object[] rVals = encoder.parseHeader(buf, offset);

        offset = ((Integer) rVals[0]).intValue();
        byte asnType = ((Byte) rVals[1]).byteValue();
        int asnLength = ((Integer) rVals[2]).intValue();

        //
        // verify the length
        //
        if ((buf.length - offset) < asnLength)
            throw new AsnDecodingException("Buffer underflow error");

        //
        // verify the ASN.1 type
        //
        if (asnType != typeId())
            throw new AsnDecodingException("Invalid ASN.1 type");

        //
        // first get the name
        //
        offset = m_name.decodeASN(buf, offset, encoder);

        //
        // get the type
        //
        m_value = SnmpUtil.getSyntaxObject(buf[offset]);
        if (m_value == null)
            throw new AsnDecodingException("Unknown ASN.1 type");

        offset = m_value.decodeASN(buf, offset, encoder);

        return offset;
    }

    /**
     * Returns a newly created duplicate object to the caller
     * 
     * @return A newly created variable
     */
    @Override
    public SnmpVarBind duplicate() {
        return new SnmpVarBind(this);
    }

    /**
     * Returns a newly created duplicate object to the caller
     * 
     * @return A newly created variable
     */
    @Override
    public Object clone() {
        return new SnmpVarBind(this);
    }

    /**
     * Converts the object to a string representation
     */
    @Override
    public String toString() {
        return m_name.toString() + " = " + m_value.toString();
    }
}
