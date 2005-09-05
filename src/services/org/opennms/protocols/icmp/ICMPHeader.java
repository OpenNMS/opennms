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
// 2003 Mar 05: Changes to support response times and more platforms.
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

package org.opennms.protocols.icmp;

import java.util.Date;
import java.util.Random;

import org.opennms.protocols.ip.OC16ChecksumProducer;

/**
 * This class defines Internet Control Message Protocol header. The header
 * defines the first 8 bytes of any ICMP message. Some ICMP messages may need to
 * override the format of the second 4 bytes, the first 4 bytes are fixed by the
 * RFC. The minimum message size for any ICMP message is 8 bytes.
 * 
 * @author Brian Weaver <weave@oculan.com>
 * @version 0.1
 * 
 */
public class ICMPHeader extends Object {
    public static final byte TYPE_ECHO_REPLY = (byte) 0;

    public static final byte TYPE_DESTINATION_UNREACHABLE = (byte) 3;

    public static final byte CODE_NETWORK_UNREACHABLE = (byte) 0;

    public static final byte CODE_HOST_UNREACHABLE = (byte) 1;

    public static final byte CODE_PROTOCOL_UNREACHABLE = (byte) 2;

    public static final byte CODE_PORT_UNREACHABLE = (byte) 3;

    public static final byte CODE_FRAGMENTATION_NEEDED = (byte) 4;

    public static final byte CODE_SOURCE_ROUTE_FAILED = (byte) 5;

    public static final byte CODE_DESTINATION_NETWORK_UNKNOWN = (byte) 6;

    public static final byte CODE_DESTINATION_HOST_UNKNOWN = (byte) 7;

    public static final byte CODE_SOURCE_HOST_ISOLATED = (byte) 8;

    public static final byte CODE_DESTINATION_NETWORK_ADMIN_PROHIBITED = (byte) 9;

    public static final byte CODE_DESTINATION_HOST_ADMIN_PROHIBITED = (byte) 10;

    public static final byte CODE_NETWORK_UNREACHABLE_FOR_TOS = (byte) 11;

    public static final byte CODE_HOST_UNREACHABLE_FOR_TOS = (byte) 12;

    public static final byte CODE_COMMUNICATIONS_ADMIN_PROHIBITIED = (byte) 13;

    public static final byte CODE_HOST_PRECEDENCE_VIOLATION = (byte) 14;

    public static final byte CODE_PRECEDENCE_CUTOFF_IN_EFFECT = (byte) 15;

    public static final byte TYPE_SOURCE_QUENCH = (byte) 4;

    public static final byte TYPE_REDIRECT = (byte) 5;

    public static final byte CODE_REDIRECT_FOR_NETWORK = (byte) 0;

    public static final byte CODE_REDIRECT_FOR_HOST = (byte) 1;

    public static final byte CODE_REDIRECT_FOR_TYPE_OF_SERVICE_AND_NETWORK = (byte) 2;

    public static final byte CODE_REDIRECT_FOR_TYPE_OF_SERVICE_AND_HOST = (byte) 3;

    public static final byte TYPE_ECHO_REQUEST = (byte) 8;

    public static final byte TYPE_ROUTER_ADVERTISEMENT = (byte) 9;

    public static final byte TYPE_ROUTER_SOLICITATION = (byte) 10;

    public static final byte TYPE_TIME_EXCEEDED = (byte) 11;

    public static final byte CODE_TTL_EQ_ZERO_IN_TRANSIT = (byte) 0;

    public static final byte CODE_TTL_EQ_ZERO_IN_REASSEMBLY = (byte) 1;

    public static final byte TYPE_PARAMETER_PROBLEM = (byte) 12;

    public static final byte CODE_BAD_IP_HEADER = (byte) 0;

    public static final byte CODE_REQUIRED_OPTION_MISSING = (byte) 1;

    public static final byte TYPE_TIMESTAMP_REQUEST = (byte) 13;

    public static final byte TYPE_TIMESTAMP_REPLY = (byte) 14;

    public static final byte TYPE_INFORMATION_REQUEST = (byte) 15;

    public static final byte TYPE_INFORMATION_REPLY = (byte) 16;

    public static final byte TYPE_ADDRESS_MASK_REQUEST = (byte) 17;

    public static final byte TYPE_ADDRESS_MASK_REPLY = (byte) 18;

    private byte m_type; // ECHO_REQUEST, ECHO_REPLY, etc...

    private byte m_code; // echo code

    private short m_checksum; // 16-bit one's complement checksum

    private short m_ident; // 16-bit identity

    private short m_sequence; // 16-bit sequence

    /**
     * Used to generate the sequence numbers for the class. This number is
     * generated application wide.
     * 
     */
    private static short sm_seq = 0;

    /**
     * Returns the next 16-bit sequence identifier for the class. The method is
     * synchronized to prevent duplicate identifiers from being issued.
     * depending on the number of classes and how often the method is called it
     * will eventually wrap. When the value wraps back to zero, a random number
     * is generated and may cause a collision with an existing identifier. The
     * probability is low, but possible.
     * 
     * @return The next 16-bit sequence number, may be negative.
     */
    public final static synchronized short nextSequenceId() {
        if (sm_seq == 0) {
            Date d = new Date();
            Random r = new Random(d.getTime());
            sm_seq = (short) (r.nextInt());
        }
        return ++sm_seq;
    }

    /**
     * Converts a byte to a short.
     * 
     * @param b
     *            The byte to convert.
     * 
     * @return The converted byte.
     * 
     */
    protected static short byteToShort(byte b) {
        short s = (short) b;
        if (s < 0)
            s += 256;
        return s;
    }

    /**
     * Converts a byte to an integer.
     * 
     * @param b
     *            The byte to convert.
     * 
     * @return The converted byte.
     * 
     */
    protected static int byteToInt(byte b) {
        int i = (int) b;
        if (i < 0)
            i += 256;
        return i;
    }

    /**
     * Initializes the header to a default value.
     */
    public ICMPHeader() {
        m_type = 0;
        m_code = 0;
        m_checksum = 0;
        m_ident = 0;
        m_sequence = 0;
    }

    /**
     * Initializes the header using the specified type.
     * 
     * @param type
     *            The header type.
     * 
     */
    public ICMPHeader(byte type) {
        this();
        m_type = type;
    }

    /**
     * Initializes the header with the specified type and code values.
     * 
     * @param type
     *            The type value for the header
     * @param code
     *            The code value for the header
     * 
     */
    public ICMPHeader(byte type, byte code) {
        this(type);
        m_code = code;
    }

    /**
     * Constructs an ICMP header with the specified header fields.
     * 
     * @param type
     *            The 8-bit ICMP type.
     * @param code
     *            The 8-bit ICMP code.
     * @param checksum
     *            The 16-bit checksum header.
     * @param identity
     *            The 16-bit identity (user).
     * @param sequence
     *            The 16-bit sequence id.
     * 
     */
    public ICMPHeader(byte type, byte code, short checksum, short identity, short sequence) {
        m_type = type;
        m_code = code;
        m_checksum = checksum;
        m_ident = identity;
        m_sequence = sequence;
    }

    /**
     * Constructs a duplicate ICMP header that is identical to the passed
     * ICMPHeader object.
     * 
     * @param second
     *            The object to duplicate.
     * 
     */
    public ICMPHeader(ICMPHeader second) {
        m_type = second.m_type;
        m_code = second.m_code;
        m_checksum = second.m_checksum;
        m_sequence = second.m_sequence;
        m_ident = second.m_ident;
    }

    /**
     * <P>
     * Constructs a new ICMP header based upon the data contained in the buffer.
     * The buffer is decode in network byte ordering (big-endin) and must be at
     * least a {@link #getNetworkSize minimum}number of bytes available to be
     * decoded.
     * </P>
     * 
     * <P>
     * If there is an insufficent amount of data to decode a header then an
     * exception is thrown.
     * </P>
     * 
     * @param data
     *            The data buffer containing the header
     * @param offset
     *            The offset of the header in the buffer
     * 
     * @exception java.lang.IndexOutOfBoundsException
     *                Thrown if there is not enough data to construct the
     *                header.
     * 
     */
    public ICMPHeader(byte[] data, int offset) {
        if ((data.length - offset) < getNetworkSize())
            throw new IndexOutOfBoundsException("Insufficient number of bytes available to construct the ICMP header");

        m_type = data[offset++];
        m_code = data[offset++];
        m_checksum = (short) (byteToShort(data[offset++]) << 8 | byteToShort(data[offset++]));
        m_sequence = (short) (byteToShort(data[offset++]) << 8 | byteToShort(data[offset++]));
        m_ident = (short) (byteToShort(data[offset++]) << 8 | byteToShort(data[offset++]));
    }

    /**
     * Returns the 8-bit type code for the ICMP packet.
     * 
     * @return The ICMP type.
     */
    public final byte getType() {
        return m_type;
    }

    /**
     * Sets the 8-bit type code for the packet.
     * 
     * @param type
     *            The new ICMP type.
     * 
     */
    protected void setType(byte type) {
        m_type = type;
    }

    /**
     * Returns the 8-bit code for the ICMP packet.
     * 
     * @return The ICMP code.
     * 
     */
    public final byte getCode() {
        return m_code;
    }

    /**
     * Sets the 8-bit code for the ICMP packet
     * 
     * @param code
     *            The new ICMP code.
     * 
     */
    public final void setCode(byte code) {
        m_code = code;
    }

    /**
     * Returns the sequence identifier for the ICMP header.
     * 
     * @return The 16-bit sequence identifier.
     * 
     */
    public final short getSequenceId() {
        return m_sequence;
    }

    /**
     * Gets the next global identifier and sets the value in the object. In
     * addition the new sequence identifier is returned.
     * 
     * @return The new 16-bit sequence identifier.
     * 
     */
    public final short setNextSequenceId() {
        m_sequence = nextSequenceId();
        return m_sequence;
    }

    /**
     * Sets the headers 16-bit sequence identifier.
     * 
     * @param id
     *            The new 16-bit sequence id.
     * 
     */
    public final void setSequenceId(short id) {
        m_sequence = id;
    }

    /**
     * Used to get the headers user defined identity.
     * 
     * @return The 16-bit identity.
     * 
     */
    public final short getIdentity() {
        return m_ident;
    }

    /**
     * Sets the header's 16-bit user defined identity value.
     * 
     * @param identity
     *            The header's new identity.
     * 
     */
    public final void setIdentity(short identity) {
        m_ident = identity;
    }

    /**
     * Used to retrieve the current checksum for the header. This is the last
     * checksum computed for the header (or it's derived classes). To compute
     * the new checksum a call to the method computeChecksum() must be called.
     * 
     * @return The 16-bit one's compliment checksum.
     * 
     */
    public final short getChecksum() {
        return m_checksum;
    }

    protected void setChecksum(short sum) {
        m_checksum = sum;
    }

    /**
     * Provides the default checksum implementation for the ICMP header. It MUST
     * be overriden by any derived classes to ensure that the checksum includes
     * the dervived class data. Once the checksum is calculated, a call to the
     * method getChecksum() will return the calculated sum.
     */
    public void computeChecksum() {
        OC16ChecksumProducer summer = new OC16ChecksumProducer();
        computeChecksum(summer);
        m_checksum = summer.getChecksum();
    }

    /**
     * Used by derived classes to begin the checksum process. The process
     * involves setting the checksum to zero and then performing the checksum
     * over the various values.
     * 
     * @param summer
     *            The checksum builder object.
     */
    protected void computeChecksum(OC16ChecksumProducer summer) {
        summer.reset();
        summer.add(m_type, m_code);

        // adding zero has should no effect....
        // m_checksum = 0;
        summer.add((short) 0);

        summer.add(m_sequence);
        summer.add(m_ident);
    }

    /**
     * Writes the ICMP header out to the specified buffer at the starting
     * offset. If the buffer does not have sufficent data to store the
     * information then an IndexOutOfBoundsException is thrown.
     * 
     * @param buf
     *            The storage buffer.
     * @param offset
     *            The location to start in buf.
     * 
     * @return The new offset after storing to the buffer.
     * 
     * @exception IndexOutOfBoundsException
     *                Thrown if the buffer does not have enough storage space.
     * 
     */
    protected int storeToBuffer(byte[] buf, int offset) {
        if (buf.length < (offset + 8))
            throw new IndexOutOfBoundsException("Array index overflow in buffer");

        buf[offset++] = (byte) (m_type);
        buf[offset++] = (byte) (m_code);
        buf[offset++] = (byte) ((m_checksum >>> 8) & 0xff);
        buf[offset++] = (byte) (m_checksum & 0xff);
        buf[offset++] = (byte) ((m_ident >>> 8) & 0xff);
        buf[offset++] = (byte) (m_ident & 0xff);
        buf[offset++] = (byte) ((m_sequence >>> 8) & 0xff);
        buf[offset++] = (byte) (m_sequence & 0xff);

        return offset;
    }

    /**
     * Reads the ICMP header from the specified buffer and sets the internal
     * fields equal to the data. If the buffer does not have sufficent data to
     * restore the header then an IndexOutOfBoundsExceptoin is thrown by the
     * method.
     * 
     * @param buf
     *            The buffer to read the data from.
     * @param offset
     *            The offset to start reading data.
     * 
     * @return The new offset after reading the data.
     * 
     * @exception IndexOutOfBoundsException
     *                is thrown if there is not sufficent data in the buffer.
     * 
     */
    protected int loadFromBuffer(byte[] buf, int offset) {
        if (buf.length < (offset + 8))
            throw new IndexOutOfBoundsException("Insufficient data to load ICMP header");

        m_type = (byte) (buf[offset++]);
        m_code = (byte) (buf[offset++]);
        m_checksum = (short) (byteToShort(buf[offset++]) << 8 | byteToShort(buf[offset++]));

        m_ident = (short) (byteToShort(buf[offset++]) << 8 | byteToShort(buf[offset++]));

        m_sequence = (short) (byteToShort(buf[offset++]) << 8 | byteToShort(buf[offset++]));

        return offset;
    }

    /**
     * Returns the number of bytes required to read/write an icmp header. A
     * header is composed of a fixed size number of byte. This is not expected
     * to change, but this method allows derived classes to query the number of
     * bytes for reading/writing an icmp header. Thus should the standard ever
     * change, the derived class should be able to dynamically handle the
     * change.
     * 
     * @return The number of bytes required to read/write an icmp header.
     * 
     */
    public static int getNetworkSize() {
        return 8;
    }

    /**
     * Used to test to see if the header is an echo reply message. If it is an
     * echo reply message then true is returned.
     * 
     * @return True if the header marks an echo reply.
     * 
     */
    public final boolean isEchoReply() {
        return (m_type == TYPE_ECHO_REPLY);
    }

    /**
     * Used to test to see if the header is an echo request message. If it is an
     * echo request message then true is returned.
     * 
     * @return True if the header marks an echo request.
     * 
     */
    public final boolean isEchoRequest() {
        return (m_type == TYPE_ECHO_REQUEST);
    }

    /**
     * Converts the object to a string of bytes.
     */
    public byte[] toBytes() {
        byte[] b = new byte[8];
        storeToBuffer(b, 0);
        return b;
    }
}
