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
// Modifications:
//
// 2007 Jun 23: Java 5 generics. - dj@opennms.org
// 2003 Mar 20: Added code to allow the joeSNMP library to answer SNMP requests
//              to be used in creating SNMP agents.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//
// SnmpPduPacket.java,v 1.1.1.1 2001/11/11 17:27:22 ben Exp
//
//

//
// Log:
//	05/24/00 - Weave
//		Fixed type-o where V2TRAP & V2REPORT were wrapped
//		using 265 instead of 256 as was correct.
//
//	07/18/00 - Weave
//		Fixed an error in the protected constructor found by
//		Naz Irizarry. The ctor was using ArrayList.set(...) to
//		duplicate the list when it should have been using
//		ArrayList.add(...). You cannot call set on an index until
//		it has first been added.
//

//
// RFC 1902: Structure of Management Information for SNMPv2
//
//   PDU ::= 
//    SEQUENCE {
//      request-id   INTEGER32
//      error-status INTEGER
//      error-index  INTEGER
//      Variable Bindings
//    }
//
// BulkPDU ::=
//    SEQUENCE {
//      request-id      INTEGER32
//      non-repeaters   INTEGER
//      max-repetitions INTEGER
//      Variable Bindings
//    }
//

//
// RFC 1157: A Simple Network Management Protocol (SNMP)
//
//   PDU ::= 
//    SEQUENCE {
//      request-id   INTEGER
//      error-status INTEGER
//      error-index  INTEGER
//      Variable Bindings
//    }
//
//   TrapPDU ::=
//    SEQUENCE {
//      enterprise    OBJECTID
//	agent-address NetworkAddress
//      generic-trap  INTEGER
//      specific-trap INTEGER
//      time-stamp    TIMETICKS
//      Variable Bindings
//    }
//

package org.opennms.protocols.snmp;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.opennms.protocols.snmp.asn1.ASN1;
import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * Base class for all Protocol Data Unit (PDU) implementations. The class
 * defines methods to handle most v1 and v2 implementation of SNMP with only
 * minor work needed by the derived class.
 *
 * @see SnmpPduRequest
 * @see SnmpPduBulk
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @version 1.1.1.1
 */
public abstract class SnmpPduPacket extends Object implements SnmpSyntax, Cloneable {
    /**
     * The static variable for the class. This variable is used to get a
     * "unique" sequence number for each PDU
     */
    private static int sm_seq = 0;

    /**
     * The SNMP command for the pdu. See the list of command later on in the
     * module for more information.
     * 
     * @see SnmpPduPacket#GET
     * @see SnmpPduPacket#GETNEXT
     * @see SnmpPduPacket#SET
     * @see SnmpPduPacket#RESPONSE
     * @see SnmpPduPacket#INFORM
     * @see SnmpPduPacket#REPORT
     * @see SnmpPduPacket#GETBULK
     */
    private int m_command; // from pdu

    /**
     * The request id for this specific packet.
     */
    private int m_requestId; // from pdu

    /**
     * The peer of this packet, if we are agent
     */
    private SnmpPeer m_peer = null;

    /**
     * The list of variables for this particular PDU. The list may be quite
     * large so long as the packet can be received by the remote appliction
     */
    private ArrayList<SnmpVarBind> m_variables; // from pdu

    /**
     * The error status in a normal pdu, is is used as the non-repeaters in the
     * getbulk.
     * 
     * @see SnmpPduRequest
     * @see SnmpPduBulk
     */
    protected int m_errStatus; // is non-repeaters in Bulk!

    /**
     * The error index in a normal pdu, it is used as the maximum repititions in
     * the get bulk pdu.
     * 
     * @see SnmpPduRequest
     * @see SnmpPduBulk
     */
    protected int m_errIndex; // is max-repition in Bulk!

    /**
     * Default class constructor. Initialzies all primitive members to zero, and
     * allocates a new array list for the variables.
     */
    protected SnmpPduPacket() {
        m_command = 0;
        m_requestId = 0;
        m_errStatus = 0;
        m_errIndex = 0;
        m_variables = new ArrayList<SnmpVarBind>();
    }

    /**
     * Class copy constructor. Constructs the object with all the same values as
     * the passed packet. The variables are duplicated into a new array so that
     * changes to the source pdu will not affect the newly create pdu.
     *
     * @param second
     *            The source pdu to copy values from.
     */
    protected SnmpPduPacket(SnmpPduPacket second) {
        m_command = second.m_command;
        m_requestId = second.m_requestId;
        m_errStatus = second.m_errStatus;
        m_errIndex = second.m_errIndex;

        int sz = second.m_variables.size();
        m_variables = new ArrayList<SnmpVarBind>(sz);

        for (int x = 0; x < sz; x++) {
            m_variables.add(x, second.m_variables.get(x).duplicate());
        }
    }

    /**
     * creates a new pdu with the command set to the passed value.
     *
     * @param command
     *            The type of pdu packet.
     */
    protected SnmpPduPacket(int command) {
        this();
        m_command = command;
    }

    /**
     * Creates a new pdu with the spcified command and the list of variables.
     *
     * @param command
     *            The type of pdu packet.
     * @param vars
     *            The variable list for the pdu.
     */
    protected SnmpPduPacket(int command, SnmpVarBind[] vars) {
        this(command);
        if (vars != null) {
            m_variables.ensureCapacity(vars.length);
            for (int x = 0; x < vars.length; x++) {
                m_variables.add(vars[x].duplicate());
            }
        }
    }

    /**
     * Use to sequence the all pdu request across the entire library. If the
     * sequence id is equal to zero then a random number generator is created
     * and is used to seed the sequence.
     *
     * @return The new sequnce identifier
     */
    public static synchronized int nextSequence() {
        if (sm_seq == 0) {
            Date seed = new Date();
            Random rnd = new Random(seed.getTime());
            sm_seq = rnd.nextInt(1000000);
        }
        return sm_seq++;
    }

    //
    // V1 commands
    //
    // Add 256 to make them unsigned quantities!
    // ASN.CONTEXT is equal 0x80 and will be sign extended. The
    // value of 256 will cause the value to be appropiately unsigned!
    //

    /**
     * Defines a SNMPv1 Get Request PDU message.
     */
    public static final int GET = (int) (ASN1.CONTEXT | ASN1.CONSTRUCTOR | 0) + 256;

    /**
     * Defines a SNMPv1 Get Next Request PDU message.
     */
    public static final int GETNEXT = (int) (ASN1.CONTEXT | ASN1.CONSTRUCTOR | 1) + 256;

    /**
     * Defines a SNMPv1 Response PDU message.
     */
    public static final int RESPONSE = (int) (ASN1.CONTEXT | ASN1.CONSTRUCTOR | 2) + 256;

    /**
     * Defines a SNMPv1 PDU Set Request message. The set request uses the
     * write-only string from the session. All others use the read-only
     * community string.
     */
    public static final int SET = (int) (ASN1.CONTEXT | ASN1.CONSTRUCTOR | 3) + 256;

    //
    // V2 commands
    //
    // Add 256 to make them unsigned quantities!
    // ASN.CONTEXT is equal 0x80 and will be sign extended. The
    // value of 256 will cause the value to be appropiately unsigned!
    //
    /**
     * Defines a SNMPv2 Get Bulk Request message.
     */
    public static final int GETBULK = (int) (ASN1.CONTEXT | ASN1.CONSTRUCTOR | 5) + 256;

    /**
     * Defines a SNMPv2 Inform Request message
     */
    public static final int INFORM = (int) (ASN1.CONTEXT | ASN1.CONSTRUCTOR | 6) + 256;

    /**
     * Defines a SNMPv2 Trap message
     */
    public static final int V2TRAP = (int) (ASN1.CONTEXT | ASN1.CONSTRUCTOR | 7) + 256;

    /**
     * Defines a SNMPv2 Report message.
     */
    public static final int REPORT = (int) (ASN1.CONTEXT | ASN1.CONSTRUCTOR | 8) + 256;

    //
    // V1 errors
    //
    /**
     * No error occured in the request. Also known as a successful request.
     */
    public static final int ErrNoError = 0;

    /**
     * The PDU was too large for the agent to process
     */
    public static final int ErrTooBig = 1;

    /**
     * There was no such object identifier defined in the agent's tables.
     */
    public static final int ErrNoSuchName = 2;

    /**
     * If the object type does not match the object value in the agent's tables.
     */
    public static final int ErrBadValue = 3;

    /**
     * Attempting to set a read-only object in the agent's tables.
     */
    public static final int ErrReadOnly = 4;

    /**
     * A generic SNMPv1 error occured.
     */
    public static final int ErrGenError = 5;

    //
    // V2 Errors
    //
    /**
     * The specified SET request could not access the specified instance.
     */
    public static final int ErrNoAccess = 6;

    /**
     * The specified object is not the correct type.
     */
    public static final int ErrWrongType = 7;

    /**
     * The specified object is not the correct length.
     */
    public static final int ErrWrongLength = 8;

    /**
     * The specified object is not correctly encoded.
     */
    public static final int ErrWrongEncoding = 9;

    /**
     * The specified object doe not have the correct value.
     */
    public static final int ErrWrongValue = 10;

    /**
     * The manager does not have the permission to create the specified
     * object(s).
     */
    public static final int ErrNoCreation = 11;

    /**
     * The specified value are not consistant.
     */
    public static final int ErrInconsistentValue = 12;

    /**
     * The requested resource are not available.
     */
    public static final int ErrResourceUnavailable = 13;

    /**
     * Unable to commit the required values.
     */
    public static final int ErrCommitFailed = 14;

    /**
     * Unable to perform the undo request
     */
    public static final int ErrUndoFailed = 15;

    /**
     * The authorization failed.
     */
    public static final int ErrAuthorizationError = 16;

    /**
     * The specified instance or table is not writable
     */
    public static final int ErrNotWritable = 17;

    /**
     * The passed object identifier is not consistent.
     */
    public static final int ErrInconsistentName = 18;

    /**
     * Returns the type of PDU.
     *
     * @return The current PDU command
     */
    public int getCommand() {
        return m_command;
    }

    /**
     * Sets the PDU's current command
     *
     * @param cmd
     *            The new command.
     */
    public void setCommand(int cmd) {
        m_command = cmd;
    }

    /**
     * Returns the current request id for this packet.
     *
     * @return The sequence identifier
     */
    public int getRequestId() {
        return m_requestId;
    }

    /**
     * Sets the Peer for the Packet
     *
     * @param peer
     *            The peer of this packet
     */
    public void setPeer(SnmpPeer peer) {
        m_peer = peer;
    }

    /**
     * Returns the current peer for this packet.
     *
     * @return The peer or null, if its a own request
     */
    public SnmpPeer getPeer() {
        return m_peer;
    }

    /**
     * Sets the protocol data unit's sequence identifer
     *
     * @param reqid
     *            The new request id
     */
    public void setRequestId(int reqid) {
        m_requestId = reqid;
    }

    /**
    /**
     * Returns the number of variables in the data unit.
     *
     * @return The number of variables.
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
     */
    public void addVarBindAt(int ndx, SnmpVarBind vb) {
        m_variables.add(ndx, vb);
    }

    /**
     * Retrieves the variable at the specific index.
     *
     * @param ndx
     *            The index of the variable
     * @return The variable at the specified index
     */
    public SnmpVarBind getVarBindAt(int ndx) {
        return ((SnmpVarBind) (m_variables.get(ndx)));
    }

    /**
     * Sets the specific variable at the requested location.
     *
     * @param ndx
     *            The location to set
     * @param vb
     *            The new variable
     */
    public void setVarBindAt(int ndx, SnmpVarBind vb) {
        m_variables.set(ndx, vb);
    }

    /**
     * Removes the variable as defined by the index
     *
     * @param ndx
     *            The index of the variable to remove
     * @return The removed variable
     */
    public SnmpVarBind removeVarBindAt(int ndx) {
        return (SnmpVarBind) m_variables.remove(ndx);
    }

    /**
     * Returns a list of all the variables managed by this protocol data unit.
     *
     * @return An array of the internal variable.
     */
    public SnmpVarBind[] toVarBindArray() {
        Object[] list = m_variables.toArray();
        SnmpVarBind[] vblist = new SnmpVarBind[list.length];
        for (int x = 0; x < list.length; x++) {
            vblist[x] = (SnmpVarBind) (list[x]);
        }
        return vblist;
    }

    /**
     * Returns the PDU commmand in an 8-bit format
     *
     * @return The pdu command
     */
    public byte typeId() {
        return (byte) (m_command & 0xff);
    }

    /**
     * {@inheritDoc}
     *
     * Encodes the protocol data unit using the passed encoder and stores the
     * results in the passed buffer. An exception is thrown if an error occurs
     * with the encoding of the information.
     * @exception AsnEncodingException
     *                Thrown if the encoder finds an error in the buffer.
     */
    public int encodeASN(byte[] buf, int offset, AsnEncoder encoder) throws AsnEncodingException {
        int begin = offset;

        //
        // encode the request id, error status (non-repeaters),
        // and error index (max-repititions).
        //
        SnmpInt32 val = new SnmpInt32(m_requestId);
        offset = val.encodeASN(buf, offset, encoder);
        val.setValue(m_errStatus);
        offset = val.encodeASN(buf, offset, encoder);
        val.setValue(m_errIndex);
        offset = val.encodeASN(buf, offset, encoder);

        //
        // mark the beginning of the vblist
        //
        int vbbegin = offset;

        //
        // Now encode the SnmpVarBinds!
        //
        int sz = m_variables.size();
        for (int x = 0; x < sz; x++) {
            SnmpVarBind ref = (SnmpVarBind) m_variables.get(x);
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
     * {@inheritDoc}
     *
     * Decodes the protocol data unit from the passed buffer. If an error occurs
     * during the decoding sequence then an AsnDecodingException is thrown by
     * the method. The value is decoded using the AsnEncoder passed to the
     * object.
     * @exception AsnDecodingException
     *                Thrown by the encoder if an error occurs trying to decode
     *                the data buffer.
     */
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
        m_command = cmd;

        //
        // get an 32-bit integer to decode values
        //
        SnmpInt32 val = new SnmpInt32();

        offset = val.decodeASN(buf, offset, encoder);
        m_requestId = val.getValue();

        offset = val.decodeASN(buf, offset, encoder);
        m_errStatus = val.getValue();

        offset = val.decodeASN(buf, offset, encoder);
        m_errIndex = val.getValue();

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

    /**
     * Defined for derived classes to return a duplicate of self. This method
     * not defined.
     *
     * @return a {@link org.opennms.protocols.snmp.SnmpSyntax} object.
     */
    public abstract SnmpSyntax duplicate();

    /**
     * Defined for derived classes to return a duplicate of self. This method
     * not defined.
     *
     * @return a {@link java.lang.Object} object.
     */
    public abstract Object clone();
}
