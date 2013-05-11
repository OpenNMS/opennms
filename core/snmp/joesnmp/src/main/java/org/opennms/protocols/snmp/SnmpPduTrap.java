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

import java.util.ArrayList;

import org.opennms.protocols.snmp.asn1.ASN1;
import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * The SnmpPduTrap object represents the SNMP Protocol Data Unit for an SNMP
 * Trap. The PDU format for a TRAP is not similar to the PDU format for other V1
 * types, and thus the SnmpPduTrap object does not extend the SnmpPduPacket
 * class.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @version 1.1.1.1
 * 
 */
public class SnmpPduTrap extends Object implements SnmpSyntax, Cloneable {
    /**
     * The trap's enterprise object identifier
     */
    private SnmpObjectId m_enterprise;

    /**
     * The IP Address of the remote agent sending the trap.
     */
    private SnmpIPAddress m_agentAddr;

    /**
     * The generic trap number.
     */
    private int m_generic;

    /**
     * The specific trap number.
     */
    private int m_specific;

    /**
     * The timestamp for when the trap occured. This should be the sysUpTime
     * from the remote system.
     */
    private long m_tstamp;

    /**
     * The list of variable bindings for the trap.
     */
    private ArrayList<SnmpVarBind> m_variables;

    /**
     * The ASN.1 type for the SNMPv1 Trap.
     */
    public final static int TRAP = (int) (ASN1.CONTEXT | ASN1.CONSTRUCTOR | 4) + 256;

    /**
     * Generic trap type: cold start.
     */
    public final static int GenericColdStart = 0;

    /**
     * Generic trap type: warm start.
     */
    public final static int GenericWarmStart = 1;

    /**
     * Generic trap type: link down.
     */
    public final static int GenericLinkDown = 2;

    /**
     * Generic trap type: link up.
     */
    public final static int GenericLinkUp = 3;

    /**
     * Generic trap type: authentication-failure.
     */
    public final static int GenericAuthenticationFailure = 4;

    /**
     * Generic trap type: EGP Neighbor Loss.
     */
    public final static int GenericEgpNeighborLoss = 5;

    /**
     * Generic trap type: Enterprise Specific.
     */
    public final static int GenericEnterpriseSpecific = 6;

    /**
     * Constructs a new SnmpPduTrap with the default values.
     * 
     */
    public SnmpPduTrap() {
        m_enterprise = new SnmpObjectId();
        m_agentAddr = new SnmpIPAddress();
        m_generic = 0;
        m_specific = 0;
        m_tstamp = 0L;
        m_variables = new ArrayList<SnmpVarBind>();
    }

    /**
     * Constructs a new trap pdu that is identical to the passed pdu.
     * 
     * @param second
     *            The object to copy.
     * 
     */
    protected SnmpPduTrap(SnmpPduTrap second) {
        m_enterprise = second.m_enterprise;
        m_agentAddr = second.m_agentAddr;
        m_generic = second.m_generic;
        m_specific = second.m_specific;
        m_tstamp = second.m_tstamp;
        m_variables = new ArrayList<SnmpVarBind>(second.m_variables.size());
        for (int x = 0; x < second.m_variables.size(); x++) {
            m_variables.add(second.m_variables.get(x).duplicate());
        }
    }

    /**
     * Used to get the enterpise identifier of the trap.
     * 
     */
    public SnmpObjectId getEnterprise() {
        return m_enterprise;
    }

    /**
     * Sets the enterprise identifier for the trap.
     * 
     * @param id
     *            The object identifier.
     */
    public void setEnterprise(SnmpObjectId id) {
        m_enterprise = (SnmpObjectId) id.clone();
    }

    /**
     * Sets the enterprise identifier for the trap. The string must be in the
     * format of a dotted decimal object identifier.
     * 
     * @param id
     *            The new identifier.
     * 
     */
    public void setEnterprise(String id) {
        m_enterprise = new SnmpObjectId(id);
    }

    /**
     * Gets the remote agent's IP address.
     * 
     */
    public SnmpIPAddress getAgentAddress() {
        return m_agentAddr;
    }

    /**
     * Sets the remote agent's IP address.
     * 
     * @param addr
     *            The remote agent's ip address.
     */
    public void setAgentAddress(SnmpIPAddress addr) {
        m_agentAddr = addr;
    }

    /**
     * Returns the generic code for the trap.
     */
    public int getGeneric() {
        return m_generic;
    }

    /**
     * Sets the generic code for the trap.
     * 
     * @param generic
     *            The new generic code for the trap.
     */
    public void setGeneric(int generic) {
        m_generic = generic;
    }

    /**
     * Returns the specific code for the trap.
     * 
     */
    public int getSpecific() {
        return m_specific;
    }

    /**
     * Sets the specific type for the trap.
     * 
     * @param spec
     *            The new specific identifier.
     * 
     */
    public void setSpecific(int spec) {
        m_specific = spec;
    }

    /**
     * Returns the timeticks from the trap.
     * 
     */
    public long getTimeStamp() {
        return m_tstamp;
    }

    /**
     * Set's the timeticks in the trap.
     * 
     * @param ts
     *            The timeticks for the trap.
     * 
     */
    public void setTimeStamp(long ts) {
        m_tstamp = ts;
    }

    /**
     * Returns the number of variables contained in the PDU.
     * 
     */
    public int getLength() {
        return m_variables.size();
    }

    /**
     * Adds a new variable to the protocol data unit. The variable is added at
     * the end of the list
     * 
     * @param vb
     *            The new variable to add
     */
    public void addVarBind(SnmpVarBind vb) {
        m_variables.add(vb);
    }

    /**
     * Adds a variable at a specific index.
     * 
     * @param ndx
     *            The index of the variable
     * @param vb
     *            The new variable.
     * 
     */
    public void addVarBindAt(int ndx, SnmpVarBind vb) {
        m_variables.add(ndx, vb);
    }

    /**
     * Retrieves the variable at the specific index.
     * 
     * @param ndx
     *            The index of the variable
     * 
     * @return The variable at the specified index
     * 
     */
    public SnmpVarBind getVarBindAt(int ndx) {
        return m_variables.get(ndx);
    }

    /**
     * Sets the specific variable at the requested location.
     * 
     * @param ndx
     *            The location to set
     * @param vb
     *            The new variable
     * 
     */
    public void setVarBindAt(int ndx, SnmpVarBind vb) {
        m_variables.set(ndx, vb);
    }

    /**
     * Removes the variable as defined by the index
     * 
     * @param ndx
     *            The index of the variable to remove
     * 
     * @return The removed variable
     * 
     */
    public SnmpVarBind removeVarBindAt(int ndx) {
        return m_variables.remove(ndx);
    }

    /**
     * Returns a list of all the variables managed by this protocol data unit.
     * 
     * @return An array of the internal variable.
     * 
     */
    public SnmpVarBind[] toVarBindArray() {
        return m_variables.toArray(new SnmpVarBind[m_variables.size()]);
    }

    /**
     * Returns the PDU commmand in an 8-bit format
     * 
     * @return The pdu command
     */
    @Override
    public byte typeId() {
        return (byte) (TRAP & 0xff);
    }

    /**
     * Encodes the protocol data unit using the passed encoder and stores the
     * results in the passed buffer. An exception is thrown if an error occurs
     * with the encoding of the information.
     * 
     * @param buf
     *            The buffer to write the encoded information.
     * @param offset
     *            The offset to start writing information
     * @param encoder
     *            The encoder object.
     * 
     * @return The offset of the byte immediantly after the last encoded byte.
     * 
     * @exception AsnEncodingException
     *                Thrown if the encoder finds an error in the buffer.
     */
    @Override
    public int encodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnEncodingException {
        int begin = offset;

        //
        // encode the enterprise id & address
        //
        offset = m_enterprise.encodeASN(buf, offset, encoder);
        offset = m_agentAddr.encodeASN(buf, offset, encoder);
        //
        // encode the request id, error status (non-repeaters),
        // and error index (max-repititions).
        //
        {
            SnmpInt32 val = new SnmpInt32(m_generic);
            offset = val.encodeASN(buf, offset, encoder);
            val.setValue(m_specific);
            offset = val.encodeASN(buf, offset, encoder);
        }

        //
        // next is the timestamp
        //
        {
            SnmpTimeTicks val = new SnmpTimeTicks(m_tstamp);
            offset = val.encodeASN(buf, offset, encoder);
        }

        //
        // mark the beginning of the vblist
        //
        int vbbegin = offset;

        //
        // Now encode the SnmpVarBinds!
        //
        int sz = m_variables.size();
        for (int x = 0; x < sz; x++) {
            SnmpVarBind ref = m_variables.get(x);
            offset = ref.encodeASN(buf, offset, encoder);
        }

        //
        // now mark the end of the varbinds
        //
        int pivot = offset;

        //
        // build the header for the varbind list
        //
        offset = encoder.buildHeader(buf, offset, SnmpVarBind.ASNTYPE, pivot - vbbegin);

        //
        // rotate the varbind header to the front.
        // Then reset the pivot point
        //
        SnmpUtil.rotate(buf, vbbegin, pivot, offset);
        pivot = offset;

        //
        // Now encode the header for the PDU,
        // then rotate the header to the front.
        //
        offset = encoder.buildHeader(buf, offset, typeId(), pivot - begin);
        SnmpUtil.rotate(buf, begin, pivot, offset);

        return offset;
    }

    /**
     * Decodes the protocol data unit from the passed buffer. If an error occurs
     * during the decoding sequence then an AsnDecodingException is thrown by
     * the method. The value is decoded using the AsnEncoder passed to the
     * object.
     * 
     * @param buf
     *            The encode buffer
     * @param offset
     *            The offset byte to begin decoding
     * @param encoder
     *            The decoder object.
     * 
     * @return The index of the byte immediantly after the last decoded byte of
     *         information.
     * 
     * @exception AsnDecodingException
     *                Thrown by the encoder if an error occurs trying to decode
     *                the data buffer.
     */
    @Override
    public int decodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnDecodingException {
        Object[] rVals = encoder.parseHeader(buf, offset);

        offset = ((Integer) rVals[0]).intValue();
        int cmd = ((Byte) rVals[1]).intValue();
        int length = ((Integer) rVals[2]).intValue();
        int begin = offset;

        //
        // set the command
        //
        if (cmd < 0)
            cmd += 256; // wrap the value to a positive quantity!
        if (TRAP != cmd)
            throw new AsnDecodingException("Invalid SNMP command, Not a Trap");

        offset = m_enterprise.decodeASN(buf, offset, encoder);
        offset = m_agentAddr.decodeASN(buf, offset, encoder);

        //
        // get an 32-bit integer to decode values
        //
        {
            SnmpInt32 val = new SnmpInt32();

            offset = val.decodeASN(buf, offset, encoder);
            m_generic = val.getValue();

            offset = val.decodeASN(buf, offset, encoder);
            m_specific = val.getValue();
        }

        //
        // Get the timestamp
        //
        {
            SnmpTimeTicks val = new SnmpTimeTicks();
            offset = val.decodeASN(buf, offset, encoder);
            m_tstamp = val.getValue();
        }

        //
        // get the total length of all
        // the variables
        //
        rVals = encoder.parseHeader(buf, offset);
        offset = ((Integer) rVals[0]).intValue();
        length = ((Integer) rVals[2]).intValue();
        byte asnType = ((Byte) rVals[1]).byteValue();

        //
        // check the ASN.1 type
        // 
        if (asnType != SnmpVarBind.ASNTYPE)
            throw new AsnDecodingException("Invalid SNMP variable list");

        //
        // set the beginning
        //
        begin = offset;

        //
        // clean out the current variables
        //
        m_variables.clear();

        //
        // decode the SnmpVarBinds
        //
        SnmpVarBind vb = new SnmpVarBind();
        while (length > 0) {
            offset = vb.decodeASN(buf, offset, encoder);
            length -= (offset - begin);
            begin = offset;

            //
            // add the varbind
            //
            m_variables.add(vb.duplicate());
        }

        return offset;
    }

    @Override
    public SnmpSyntax duplicate() {
        return new SnmpPduTrap(this);
    }

    @Override
    public Object clone() {
        return new SnmpPduTrap(this);
    }

}
