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

import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * This class provides the base class for the SNMP V2 error class. In SNMPv2 an
 * agent may respond to individual variables with one of three errors:
 * End-of-Mib-View, No-Such-Instance, or No-Such-Object. Each of the error
 * conditions are derived from this base class.
 * 
 * By responding to error in this way the agent can still return valid variables
 * while informing the manager of the variables in error.
 * 
 * For more information see "SNMP, SNMPv2, SNMPv3, and RMON 1 and 2, 3rd Ed" by
 * William Stallings. (ISBN 0-201-48534-6)
 * 
 * @version 1.1.1.1
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public abstract class SnmpV2Error extends Object implements SnmpSyntax, Cloneable, Serializable {
    /**
     * Changes to define new serialzation formats.
     * 
     */
    static final long serialVersionUID = 5701182495454482383L;

    /**
     * Default constructor. Provided to give the derived classes a meaningful
     * way to initialize themself by calling super().
     * 
     * The method performs no useful function.
     */
    public SnmpV2Error() {
        // do nothing
    }

    /**
     * Copy constructor. Provided to give the derived classes a meaningful way
     * to initialize themself by calling super(second).
     * 
     * The method performs no useful fuction.
     * 
     * @param second
     *            The object to copy to self
     * 
     */
    public SnmpV2Error(SnmpV2Error second) {
        // do nothing
    }

    /**
     * Returns the ASN.1 type identifier for the SNMPv2 error. Defined abstract
     * to fulfill the interface contract.
     * 
     * @return The ASN.1 type identifier
     */
    @Override
    public abstract byte typeId();

    /**
     * Encodes the SNMPv2 error into the passed buffer using the encoder object.
     * If an encoding exception occurs then an exception is thrown.
     * 
     * @param buf
     *            The buffer to store encoded bytes
     * @param offset
     *            The start of the encoding location
     * @param encoder
     *            The encoding object
     * 
     * @return The index of the byte immedantly after the last encoded byte.
     * 
     */
    @Override
    public int encodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnEncodingException {
        return encoder.buildHeader(buf, offset, typeId(), 0);
    }

    /**
     * Decodes the passed buffer and updates the object to match the encoded
     * information. The encoded information is recovered using the encoder
     * object.
     * 
     * @param buf
     *            The encoded buffer
     * @param offset
     *            The offset of the first byte of encoded data
     * @param encoder
     *            The object used to decode the data.
     * 
     * @return The index of the byte immedantly after the last encoded byte.
     */
    @Override
    public int decodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnDecodingException {
        Object[] rVals = encoder.parseHeader(buf, offset);

        if (((Byte) rVals[1]).byteValue() != typeId())
            throw new AsnDecodingException("Invalid ASN.1 type");

        if (((Integer) rVals[2]).intValue() != 0)
            throw new AsnDecodingException("Invalid ASN.1 length");

        return ((Integer) rVals[0]).intValue();
    }

    /**
     * Returns a duplicate of the current object. The duplicate object is a new
     * object and any changes will not be reflected in the source object. This
     * is identical to creating a new object using the copy constructor.
     * 
     * @return A newly created object that is a duplicate of self.
     */
    @Override
    public abstract SnmpSyntax duplicate();

    /**
     * Returns a duplicate of the current object. The duplicate object is a
     * newly created object and any changes made to the returned object will not
     * be reflected in the source.
     * 
     * @return A newly created object that is a duplicate of self
     * 
     */
    @Override
    public abstract Object clone();
}
