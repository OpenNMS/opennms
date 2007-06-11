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

package org.opennms.protocols.ip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class defines a loaded IP header object. It does not allow the user of
 * the class to set any of the values. Nor can a default object be constructed.
 * A copy of an existing header can be created or one can be loaded from a
 * collection of bytes.
 * 
 * For more information on the IP header see the book "TCP/IP Illustrated,
 * Volume 1: The Protocols" by W. Richard Stevens.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * 
 */
public class IPHeader extends Object {
    /**
     * The supported version of the IP header
     */
    public static final int IP_VERSION = 4;

    /**
     * The Type-Of-Service mask. This constant is used to mask bits that define
     * the type of service field. See RFC 791.
     */
    public static final int TOS_PRECEDENCE_MASK = 0xe0;

    /**
     * Network Critical TOS. See RFC 791.
     */
    public static final int TOS_PRECEDENCE_NETWORK_CRITICAL = 0xe0;

    /**
     * Internetworking Control TOS. See RFC 791.
     */
    public static final int TOS_PRECEDENCE_INTERNETWORK_CONTROL = 0xc0;

    /**
     * Critical/ECP TOS. See RFC 791.
     */
    public static final int TOS_PRECEDENCE_CRITICAL_ECP = 0x90;

    /**
     * Flash Override TOS. See RFC 791.
     */
    public static final int TOS_PRECEDENCE_FLASH_OVERRIDE = 0x80;

    /**
     * Flash TOS. See RFC 791.
     */
    public static final int TOS_PRECEDENCE_FLASH = 0x60;

    /**
     * Immediate TOS. See RFC 791.
     */
    public static final int TOS_PRECEDENCE_IMMEDIATE = 0x40;

    /**
     * Priority TOS. See RFC 791.
     */
    public static final int TOS_PRECEDENCE_PRIORITY = 0x20;

    /**
     * Routine TOS. See RFC 791.
     */
    public static final int TOS_PRECEDENCE_ROUTINE = 0x00;

    /**
     * TOS delay mask as defined by RFC 791.
     */
    public static final int TOS_DELAY_MASK = 0x10;

    /**
     * Minimize the delay when handling packets.
     */
    public static final int TOS_DELAY_LOW = 0x10;

    /**
     * Normal packet handling
     */
    public static final int TOS_DELAY_NORMAL = 0x00;

    /**
     * TOS Throughput mask
     */
    public static final int TOS_THROUGHPUT_MASK = 0x08;

    /**
     * High throughput requested
     */
    public static final int TOS_THROUGHPUT_HIGH = 0x08;

    /**
     * Normal throughput requested
     */
    public static final int TOS_THROUGHPUT_NORMAL = 0x00;

    /**
     * Packet reliablity mask.
     */
    public static final int TOS_RELIBILITY_MASK = 0x04;

    /**
     * High Reliability requested.
     */
    public static final int TOS_RELIBILITY_HIGH = 0x04;

    /**
     * Normal reliability requrested
     */
    public static final int TOS_RELIBILITY_NORMAL = 0x00;

    /**
     * Mask of the reseered bits.
     */
    public static final int TOS_RESERVED_MASK = 0x03;

    /**
     * The mask of the flags in the fragment field of the IP header
     */
    public static final int FLAGS_MASK = 0xe000;

    /**
     * Don't fragment datagrams field
     */
    public static final int FLAGS_DONT_FRAGMENT = 0x4000;

    /**
     * More fragments are necessary to reassemble this packet
     */
    public static final int FLAGS_MORE_FRAGMENTS = 0x2000;

    /**
     * The bit(s) that define if the optiosn are copied to each datagram when
     * (or if) it is fragmented.
     */
    public static final int OPTION_COPY_MASK = 0x80;

    /**
     * The option class mask
     */
    public static final int OPTION_CLASS_MASK = 0x60;

    /**
     * The option number mask
     */
    public static final int OPTION_NUMBER_MASK = 0x1f;

    /**
     * Option identifier for the End Of Options List option.
     */
    public static final int OPTION_ID_EOO = 0x00;

    /**
     * Option identifier for the loose source routing option
     */
    public static final int OPTION_ID_LOOSE_SOURCE_ROUTING = 0x83;

    /**
     * Option identifer for the the strict source routing option
     */
    public static final int OPTION_ID_STRICT_SOURCE_ROUTING = 0x89;

    /**
     * Option identifier for the route record option
     */
    public static final int OPTION_ID_ROUTE_RECORD = 0x07;

    /**
     * The IP version
     */
    private byte m_version; // 4-bit value

    /**
     * The length of the IP header in 32-bit words
     */
    private byte m_hdrlen; // 4-bit value

    /**
     * the Type-Of-Service defined for the IP datagram
     */
    private byte m_tos; // 8-bit type of service

    /**
     * The total length of the IP datagram and it's payload
     */
    private short m_length; // 16-bit total length of IP Packet

    /**
     * The identity of the IP dagagram.
     */
    private short m_identity; // 16-bit identification

    /**
     * The fragmentation flags tha occupy the upper 3 bits of the fragment
     * offset field
     */
    private byte m_flags; // 3-bit flags

    /**
     * The fragment offset of this packet
     */
    private short m_fragOffset; // 13-bit fragment offset

    /**
     * The Time-To-Live for this IP packet
     */
    private byte m_ttl; // 8-bit time-to-live

    /**
     * The protocol encapuslated by this packet
     */
    private byte m_protocol; // 8-bit protocol

    /**
     * One's compliment 16-bit checksum of the header only. This does not
     * include the value for the data
     */
    private short m_checksum; // 16-bit one's compliment checksum

    /**
     * Source address of the IP datagram
     */
    private int m_srcAddr; // 32-bit source address

    /**
     * Destination address of the IP datagram
     */
    private int m_dstAddr; // 32-bit destination address

    /**
     * any option data in the datagram
     */
    private byte[] m_options; // maximum of 40-bytes

    /**
     * The Option class is used as the base class for any options that are at
     * the end of the IP header.
     * 
     * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
     * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
     * 
     */
    public abstract static class Option {
        /**
         * The single byte that defiend the copied bit, class, and code for the
         * option
         */
        protected int m_code;

        /**
         * Defines the code for the End-Of-Options list
         */
        public static final int CODE_END_OF_OPTION_LIST = 0;

        /**
         * Defines the code for the loose source routing option
         */
        public static final int CODE_LOOSE_SOURCE_ROUTE = 0x83;

        /**
         * Defines the code for the strict soruce routing option
         */
        public static final int CODE_STRICT_SOURCE_ROUTE = 0x89;

        /**
         * Defines the code for the packet route record option.
         */
        public static final int CODE_ROUTE_RECORD = 0x07;

        /**
         * Class constructor that is only available to the derived classes of
         * the Option class.
         * 
         * @param code
         *            The code for the option.
         */
        protected Option(byte code) {
            m_code = (int) code & 0x000000ff;
        }

        /**
         * The nubmer of bytes required to represent this option in the IP
         * header
         * 
         * @return The bytes used by this option
         * 
         */
        abstract int bytesRequired();

        /**
         * Writes the option to the passed array, starting at the defined
         * offset. The array must have enough space or an exception is
         * generated.
         * 
         * @param dest
         *            The destination to write the data
         * @param offset
         *            The offset of the first written byte
         * 
         * @return The passed offset plus the number of required bytes.
         * 
         */
        abstract int writeBytes(byte[] dest, int offset);

        /**
         * Returns the class for the option.
         * 
         */
        public int getOptionClass() {
            return (int) m_code & OPTION_CLASS_MASK;
        }

        /**
         * Returns the option number for the instance
         */
        public int getOptionNumber() {
            return (int) m_code & OPTION_NUMBER_MASK;
        }

        /**
         * Returns true if the copy flag is set in the options header
         */
        public boolean isOptionCopied() {
            return ((m_code & OPTION_COPY_MASK) != 0);
        }
    }

    /**
     * This class is used to represent the <EM>End-Of-Option</EM> list in the
     * IP header. After this option, the option list is not processed any
     * further.
     * 
     * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
     * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
     * 
     */
    public static final class EndOfOptions extends Option {
        /**
         * Returns the number of bytes requried to represent this option
         */
        int bytesRequired() {
            return 1;
        }

        /**
         * Converts the option to an array of bytes and writes those bytes in to
         * the destiantion buffer. The bytes are written startint at the offset
         * passed to the method.
         * 
         * @param dest
         *            The destiantion buffer to write the bytes
         * @param offset
         *            The offset to start writing in the buffer
         * 
         * @return The offset plus the number of bytes written to the buffer.
         * 
         * @exception java.lang.ArrayIndexOutOfBounds
         *                Throws in there is insufficient space in the buffer.
         * 
         */
        int writeBytes(byte[] dest, int offset) {
            dest[offset++] = 0;
            return offset;
        }

        /**
         * Constructs a new End-Of-Options list instance that can be added or
         * found in the IP header.
         */
        public EndOfOptions() {
            super((byte) 0);
        }
    }

    /**
     * This class represents routing options that may be part of an IP header.
     * The route defines a set of IP addresses that a packet may have or should
     * pass though.
     * 
     * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
     * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
     * 
     */
    public static class RouteOption extends Option {
        /**
         * The list of addresses for the packet to hit on it's way to it's
         * destination
         */
        protected List<IPv4Address> m_addrs;

        /**
         * Adds an address to the end of the set of addresses to hit on its lan
         * trip
         * 
         * @param addr
         *            The address to add to the loose source route
         * 
         * @exception java.lang.IndexOutOfBoundsException
         *                Thrown if the address list is full
         */
        void add(IPv4Address addr) {
            if (m_addrs.size() == 9)
                throw new IndexOutOfBoundsException("The address could not be added, the record is full");
            m_addrs.add(addr);
        }

        /**
         * The number of bytes required to represent this option in an IP header
         */
        int bytesRequired() {
            return 3 + (4 * m_addrs.size());
        }

        /**
         * This method is used to serialized the data contained in the option to
         * the passed array, starting at the offset passed. If an insufficient
         * amount of space exists then an exception is thrown.
         * 
         * @param dest
         *            The destination buffer
         * @param offset
         *            The offset to start writing data
         * 
         * @return The new offset after writing data
         * 
         * @exception java.lang.ArrayIndexOutOfBounds
         *                Thrown if there is not sufficent space in the passed
         *                buffer.
         */
        int writeBytes(byte[] dest, int offset) {
            dest[offset++] = (byte) m_code;
            dest[offset++] = (byte) bytesRequired();
            dest[offset++] = (byte) 4;

            Iterator<IPv4Address> iter = m_addrs.iterator();
            while (iter.hasNext()) {
                int addr = ((IPv4Address) iter.next()).getAddress();
                for (int i = 3; i >= 0; i++)
                    dest[offset++] = (byte) ((addr >> (8 * i)) & 0xff);
            }

            return offset;
        }

        /**
         * Constructs a new, empty instance of the class.
         */
        RouteOption(byte code) {
            super(code);
            m_addrs = new ArrayList<IPv4Address>(9);
        }

        /**
         * Constructs a new instance of the class with the passed addresses used
         * for the routing. If the set of addresses is larger than the option
         * can hold an exception is thrown.
         * 
         * @param addrs
         *            The list of addresses for the loose source route.
         * 
         * @exception java.lang.IndexOutOfBoundsException
         *                Thrown if the number of addresses is to large for the
         *                option
         */
        RouteOption(byte code, IPv4Address[] addrs) {
            super(code);
            if (addrs.length > 9)
                throw new IndexOutOfBoundsException("Route Option List Cannot Exceed 9 Addresses");

            m_addrs = new ArrayList<IPv4Address>(9);
            for (int i = 0; i < addrs.length; i++)
                m_addrs.add(addrs[i]);
        }

        /**
         * Constructs a new instance of the class with the passed addresses used
         * for the routing. If the set of addresses is larger than the option
         * can hold an exception is thrown.
         * 
         * @param addrs
         *            The list of addresses for the loose source route.
         * 
         * @exception java.lang.IndexOutOfBoundsException
         *                Thrown if the number of addresses is to large for the
         *                option
         */
        RouteOption(byte code, List<IPv4Address> addrs) {
            super(code);
            if (addrs.size() > 9)
                throw new IndexOutOfBoundsException("Route Option List Cannot Exceed 9 Addresses");

            Iterator<IPv4Address> iter = addrs.iterator();
            m_addrs = new ArrayList<IPv4Address>(9);
            while (iter.hasNext())
                m_addrs.add(iter.next());
        }

        /**
         * Returns the iterator that may be used to look at the encapsulated
         * addresses. The class IPv4Address is used to represent the addresses
         * in the list.
         * 
         * @return An iterator that can be used to operate on the list.
         */
        public Iterator<IPv4Address> iterator() {
            return m_addrs.iterator();
        }

        /**
         * Returns the number of addresses contained in the option list.
         */
        public int size() {
            return m_addrs.size();
        }
    }

    /**
     * This class represents the loose source routing options that may be part
     * of an IP header. The loose source route defines a set of IP addresses
     * that a packet should pass though. As the packet reaches each address the
     * packet is forwarded to the next element in the route.
     * 
     * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
     * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
     * 
     */
    public static final class LooseSourceRouteOption extends RouteOption {
        /**
         * Constructs a new, empty instance of the class.
         */
        LooseSourceRouteOption() {
            super((byte) 0x83);
        }

        /**
         * Constructs a new instance of the class with the passed addresses used
         * for the routing. If the set of addresses is larger than the option
         * can hold an exception is thrown.
         * 
         * @param addrs
         *            The list of addresses for the loose source route.
         * 
         * @exception java.lang.IndexOutOfBoundsException
         *                Thrown if the number of addresses is to large for the
         *                option
         */
        public LooseSourceRouteOption(IPv4Address[] addrs) {
            super((byte) 0x83, addrs);
        }

        /**
         * Constructs a new instance of the class with the passed addresses used
         * for the routing. If the set of addresses is larger than the option
         * can hold an exception is thrown.
         * 
         * @param addrs
         *            The list of addresses for the loose source route.
         * 
         * @exception java.lang.IndexOutOfBoundsException
         *                Thrown if the number of addresses is to large for the
         *                option
         */
        public LooseSourceRouteOption(List<IPv4Address> addrs) {
            super((byte) 0x83, addrs);
        }
    }

    /**
     * This class represents the strict source routing options that may be part
     * of an IP header. The strict source route defines a set of IP addresses
     * that a packet must pass though. As the packet reaches each address the
     * packet is forwarded to the next element in the route.
     * 
     * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
     * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
     * 
     */
    public static final class StrictSourceRouteOption extends RouteOption {
        /**
         * Constructs an empty instance of this class
         */
        StrictSourceRouteOption() {
            super((byte) 0x89);
        }

        /**
         * Constructs a new instance of the class with the passed addresses used
         * for the routing. If the set of addresses is larger than the option
         * can hold an exception is thrown.
         * 
         * @param addrs
         *            The list of addresses for the loose source route.
         * 
         * @exception java.lang.IndexOutOfBoundsException
         *                Thrown if the number of addresses is to large for the
         *                option
         */
        public StrictSourceRouteOption(IPv4Address[] addrs) {
            super((byte) 0x89, addrs);
        }

        /**
         * Constructs a new instance of the class with the passed addresses used
         * for the routing. If the set of addresses is larger than the option
         * can hold an exception is thrown.
         * 
         * @param addrs
         *            The list of addresses for the loose source route.
         * 
         * @exception java.lang.IndexOutOfBoundsException
         *                Thrown if the number of addresses is to large for the
         *                option
         */
        public StrictSourceRouteOption(List<IPv4Address> addrs) {
            super((byte) 0x89, addrs);
        }
    }

    /**
     * This class represents the route record option that may be part of an IP
     * header. The strict route record records a set of IP addresses that a
     * packet has passed though. As the packet reaches each address the address
     * is added to the the next element in the route.
     * 
     * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
     * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
     * 
     */
    public static final class RouteRecordOption extends RouteOption {
        /**
         * Constructs an empty route record option
         */
        RouteRecordOption() {
            super((byte) 0x7);
        }

        /**
         * Constructs an empty route record with space for <EM>capacity</EM>
         * addresses to be recoreded. The capacity CANNOT exceed 9.
         * 
         * @param capacity
         *            The number of addresses to record, max = 9.
         * 
         */
        public RouteRecordOption(int capacity) {
            super((byte) 0x7);
            for (int i = 0; i < capacity; i++)
                add(new IPv4Address(0));
        }

        /**
         * Constructs a new instance with the give addresses set in the option
         * header
         * 
         * @param addrs
         *            The list of addresses for the loose source route.
         * 
         * @exception java.lang.IndexOutOfBoundsException
         *                Thrown if the number of addresses is to large for the
         *                option
         */
        public RouteRecordOption(IPv4Address[] addrs) {
            super((byte) 0x7, addrs);
        }

        /**
         * Constructs a new instance with the given addresses stored in the
         * option.
         * 
         * @param addrs
         *            The list of addresses for the loose source route.
         * 
         * @exception java.lang.IndexOutOfBoundsException
         *                Thrown if the number of addresses is to large for the
         *                option
         */
        public RouteRecordOption(List<IPv4Address> addrs) {
            super((byte) 0x7, addrs);
        }
    }

    /**
     * Duplicates the array of bytes.
     * 
     * @param src
     *            The source bytes to duplicate.
     * 
     * @return The duplicated array of bytes.
     */
    private byte[] dup(byte[] src) {
        byte[] cpy = null;
        if (src != null) {
            cpy = new byte[src.length];
            System.arraycopy(src, 0, cpy, 0, src.length);
        }
        return cpy;
    }

    /**
     * Converts a byte to a short, treating the byte as unsigned.
     * 
     * @param b
     *            The byte to convert.
     * 
     * @return The converted value.
     * 
     */
    private static short byteToShort(byte b) {
        short r = (short) b;
        if (r < 0)
            r += 256;
        return r;
    }

    /**
     * Converts a byte to an integer, treating the byte as unsigned.
     * 
     * @param b
     *            The byte to convert.
     * 
     * @return The converted value.
     * 
     */
    private static int byteToInt(byte b) {
        int r = (int) b;
        if (r < 0)
            r += 256;
        return r;
    }

    /**
     * Converts a short to an integer, treating the short as unsigned.
     * 
     * @param s
     *            The short to convert.
     * 
     * @return The converted value
     * 
     */
    private static int shortToInt(short s) {
        int r = (int) s;
        if (r < 0)
            r += 0x10000;
        return r;
    }

    /**
     * Constructs a basic IP header, but the header <EM>is not</EM> valid
     * until a large part of the information is configured.
     */
    public IPHeader() {
        m_hdrlen = 5;
        m_version = IP_VERSION;
        m_tos = 0;
        m_length = 20;
        m_identity = 0;
        m_flags = 0;
        m_fragOffset = 0;
        m_ttl = 30;
        m_protocol = 0;
        m_checksum = 0;
        m_srcAddr = 0;
        m_dstAddr = 0;
        m_options = new byte[0];
    }

    /**
     * Constructs a new IP Header object that is identical to the passed
     * IPHeader. The new object is a complete duplicate including the option
     * data that is copied into a newly allocated array.
     * 
     * @param second
     *            The object to duplicate.
     * 
     */
    public IPHeader(IPHeader second) {
        m_hdrlen = second.m_hdrlen;
        m_version = second.m_version;
        m_tos = second.m_tos;
        m_length = second.m_length;
        m_identity = second.m_identity;
        m_flags = second.m_flags;
        m_fragOffset = second.m_fragOffset;
        m_ttl = second.m_ttl;
        m_protocol = second.m_protocol;
        m_checksum = second.m_checksum;
        m_srcAddr = second.m_srcAddr;
        m_dstAddr = second.m_dstAddr;
        m_options = dup(second.m_options);
    }

    /**
     * Constructs a new IPHeader object from the passed data buffer. The data is
     * gathered from the buffer starting at the location marked by offset. If
     * there is not sufficent data in the buffer then an exception is thrown.
     * 
     * @param header
     *            The buffer containing the header
     * @param offset
     *            The offset into the buffer where the IP header is located.
     * 
     * @exception java.lang.IndexOutOfBoundsException
     *                This exception is thrown if the minimum number of bytes
     *                are not present to represent an IPHeader object.
     * @exception UnknownIPVersionException
     *                Thrown if the format of the version is unknown.
     * 
     */
    public IPHeader(byte[] header, int offset) {
        int length = header.length;

        if ((length - offset) < 20)
            throw new IndexOutOfBoundsException("Minimum IP header size is 20 bytes");

        //
        // now start to build the header information
        //
        int ndx = 0;

        //
        // Get the header version and header length. The
        // header version lives in the first 4 bits of the
        // header. The header length lives in the next 4 bits.
        // NOTE: The header length is the number of 32-bit
        // words in the header, so the true length is m_hdrlen * 4.
        //
        m_version = (byte) (header[offset + ndx] >>> 4);
        m_hdrlen = (byte) (header[offset + ndx] & 0xf);
        ++ndx;

        //
        // check the version number
        //
        if (m_version != 4)
            throw new UnknownIPVersionException("Unknown IP Version, version = " + m_version);

        //
        // check to make sure there is enough data now that
        // we know the total length of the header
        //
        if ((length - offset) < (m_hdrlen * 4))
            throw new IndexOutOfBoundsException("Insufficient data: buffer size = " + (length - offset) + " and header length = " + (m_hdrlen * 4));

        //
        // Now get the Type Of Service flags (8-bits)
        //
        m_tos = header[offset + ndx];
        ++ndx;

        //
        // Convert the 16-bit total length of the packet
        // in bytes.
        //
        m_length = (short) (byteToShort(header[offset + ndx]) << 8 | byteToShort(header[offset + ndx + 1]));
        ndx += 2;

        //
        // Next get the 16-bit identification field
        //
        m_identity = (short) (byteToShort(header[offset + ndx]) << 8 | byteToShort(header[offset + ndx + 1]));
        ndx += 2;

        //
        // Get the next 16-bits of information. The upper 3-bits
        // are the header flags. The lower 13-bits is the
        // fragment offset!
        //
        m_fragOffset = (short) (byteToShort(header[offset + ndx]) << 8 | byteToShort(header[offset + ndx + 1]));
        m_flags = (byte) (m_fragOffset >>> 13); // get the upper 3-bits
        m_fragOffset = (short) (m_fragOffset & 0x1fff); // mask off the upper
                                                        // 3-bits
        ndx += 2;

        //
        // The 8-bit Time To Live (TTL) is next
        //
        m_ttl = header[offset + ndx];
        ++ndx;

        //
        // The 8-bit protocol is next. This is used by the
        // OS to determine if the packet is TCP, UDP, etc al.
        //
        m_protocol = header[offset + ndx];
        ++ndx;

        //
        // Now get the 16-bit one's compliment checksum
        //
        m_checksum = (short) (byteToShort(header[offset + ndx]) << 8 | byteToShort(header[offset + ndx + 1]));
        ndx += 2;

        //
        // The 32-bit IPv4 source address is next. This is the
        // address of the sender of the packet.
        //
        m_srcAddr = byteToInt(header[offset + ndx]) << 24 | byteToInt(header[offset + ndx + 1]) << 16 | byteToInt(header[offset + ndx + 2]) << 8 | byteToInt(header[offset + ndx + 3]);
        ndx += 4;

        //
        // The 32-bit IPv4 destination address. This is the address
        // of the interface that should receive the packet.
        //
        m_dstAddr = byteToInt(header[offset + ndx]) << 24 | byteToInt(header[offset + ndx + 1]) << 16 | byteToInt(header[offset + ndx + 2]) << 8 | byteToInt(header[offset + ndx + 3]);
        ndx += 4;

        //
        // get the option data now
        //
        int hl = byteToInt(m_hdrlen) << 2; // m_hdrlen * 4 ! :)
        if (hl > ndx) {
            m_options = new byte[hl - ndx];
            int x = 0;
            while (ndx < hl) {
                m_options[x++] = header[offset + ndx++];
            }
        } else {
            m_options = new byte[0];
        }

    } // end IPHeader(byte[], int)

    /**
     * Used to retreive the current version of the IP Header. Currently only
     * version 4 is supported.
     * 
     * @return The current IP version.
     * 
     */
    public byte getVersion() {
        return m_version;
    }

    /**
     * Used to get the current length of the IP Header.
     * 
     * @return The current IP header length.
     * 
     */
    public int getHeaderLength() {
        return (4 * byteToInt(m_hdrlen));
    }

    /**
     * Retreives the current TOS field from the header.
     * 
     * @return The current TOS.
     * 
     */
    public byte getTypeOfService() {
        return m_tos;
    }

    /**
     * Sets the TOS flags for the IP header.
     * 
     * @param tos
     *            The new TOS for the IP header
     */
    public void setTypeOfService(byte tos) {
        m_tos = tos;
    }

    /**
     * Use to test individual bits in the TOS fields. If the field is set then a
     * value of true is returned. If the field is not set then a false value is
     * returned.
     * 
     * @param bit
     *            The bit to validate. Valid values are 0 - 7.
     * 
     * @return True if the bit is set, false otherwise.
     * 
     */
    public boolean getTypeOfService(int bit) {
        if (bit >= 0 && bit < 8)
            return ((m_tos & (1 << bit)) != 0);

        return false;
    }

    /**
     * Returns the length of the IP packet, including the header, in bytes.
     * 
     * @return The total packet length
     * 
     */
    public int getPacketLength() {
        return shortToInt(m_length);
    }

    /**
     * Sets the length for IP packet, including the header. When setting this
     * value the size of the IP header must be accounted for.
     * 
     * @param length
     *            The length of the IP header plus the data contained within
     */
    public void setPacketLength(short length) {
        m_length = length;
    }

    /**
     * Used to retreive the 16-bit identity of the header.
     * 
     * @return The header's identity.
     * 
     */
    public short getIdentity() {
        return m_identity;
    }

    /**
     * Sets the identity of the IP header
     * 
     * @param ident
     *            The new identity of the IP header
     */
    public void setIdentity(short ident) {
        m_identity = ident;
    }

    /**
     * Used to get the 3-bit flags from the header. The flags are located in the
     * 3 least significant bits of the returned byte.
     * 
     * @return The byte containing the three flags.
     * 
     */
    public byte getFlags() {
        return m_flags;
    }

    /**
     * Sets the flags contained in the upper 3 bits of the short value for the
     * fragmentation offset. The passed bits should occupy the lower 3 bits of
     * the passed byte.
     * 
     * @param flags
     *            The flag bits, set in the lower 3 bits of the value.
     */
    public void setFlags(byte flags) {
        m_flags = flags;
    }

    /**
     * Used to get an individual flag from the flags field. The bit must be in
     * the range of [0..3).
     * 
     * @param bit
     *            The flag to retreive.
     * 
     * @return True if the bit is set, false otherwise.
     * 
     */
    public boolean getFlag(int bit) {
        if (bit >= 0 && bit < 3)
            return ((m_flags & (1 << bit)) != 0);

        return false;
    }

    /**
     * Returns the 13-bit fragment offset field from the IP header.
     * 
     * @return The 13-bit fragment offset.
     * 
     */
    public short getFragmentOffset() {
        return m_fragOffset;
    }

    /**
     * Sets the fragmentation index for this packet
     */
    public void setFragmentOffset(short offset) {
        m_fragOffset = offset;
    }

    /**
     * Gets the 8-bit Time To Live (TTL) of the packet.
     * 
     * @return The packet's ttl.
     * 
     */
    public byte getTTL() {
        return m_ttl;
    }

    /**
     * Sets the time to live for the IP header
     */
    public void setTTL(byte ttl) {
        m_ttl = ttl;
    }

    /**
     * Gets the protocol for the IP datagram.
     * 
     * @return The 8-bit protocol field.
     * 
     */
    public byte getProtocol() {
        return m_protocol;
    }

    /**
     * Sets the protocol for the IP header.
     * 
     * @param protocol
     *            The IP protocol.
     */
    public void setProtocol(byte protocol) {
        m_protocol = protocol;
    }

    /**
     * Gets the 16-bit ones compliment checksum for the IP header.
     * 
     * @return The 16-bit ones compliment checksum.
     */
    public short getChecksum() {
        return m_checksum;
    }

    /**
     * Sets the checksum for the IP header.
     * 
     * @param sum
     *            The IP header checksum.
     * 
     */
    public void setChecksum(short sum) {
        m_checksum = sum;
    }

    /**
     * Returns the dotted decimal string address of the source IP address.
     * 
     * @return The 32-bit IPv4 address
     */
    public int getSourceAddress() {
        return m_srcAddr;
    }

    /**
     * Sets the IP headers source address.
     * 
     * @param addr
     *            The soruce address for the header.
     */
    public void setSourceAddr(int addr) {
        m_srcAddr = addr;
    }

    /**
     * Returns the dotted decimal string address of the destination IP address.
     * 
     * @return The 32-bit IPv4 address.
     */
    public int getDestinationAddress() {
        return m_dstAddr;
    }

    /**
     * Sets the IP headers destination address.
     * 
     * @param addr
     *            The destination address
     * 
     */
    public void setDestinationAddress(int addr) {
        m_dstAddr = addr;
    }

    /**
     * Retrieves the IP header options from the header. The data is treated as a
     * varaiable length of option data. The IPHeader object does not attempt to
     * interpert the data.
     * 
     * @return The IP header option data, null if there is none.
     */
    public byte[] getOptionData() {
        return m_options;
    }

    /**
     * Sets the current option data for the header.
     * 
     * @param options
     *            The new options data.
     * 
     */
    public void setOptionData(byte[] options) {
        m_options = options;
        m_hdrlen = (byte) ((20 + m_options.length) / 4);
    }

    /**
     * Returns a list of options that are associated with the IP header.
     * 
     * @return The list of current options.
     */
    public List<Option> getOptions() throws InstantiationException {
        //
        // check for null data first
        //
        if (m_options == null)
            return new ArrayList<Option>();

        //
        // Process the options
        //
        List<Option> options = new ArrayList<Option>();
        int offset = 0;
        while (offset < m_options.length) {
            switch ((int) m_options[offset++] & 0xff) {
            case Option.CODE_END_OF_OPTION_LIST:
                options.add(new EndOfOptions());
                break;

            case Option.CODE_LOOSE_SOURCE_ROUTE: {
                LooseSourceRouteOption opt = new LooseSourceRouteOption();
                int addrs = ((int) m_options[offset] & 0xff) - 3;
                offset += 2;

                for (int i = 0; i < addrs / 4; i++) {
                    int ip = 0;
                    for (int j = 0; j < 4; j++)
                        ip = ip << 8 + ((int) m_options[offset++] & 0xff);

                    opt.add(new IPv4Address(ip));
                }
                options.add(opt);
            }
                break;

            case Option.CODE_STRICT_SOURCE_ROUTE: {
                StrictSourceRouteOption opt = new StrictSourceRouteOption();
                int addrs = ((int) m_options[offset] & 0xff) - 3;
                offset += 2;

                for (int i = 0; i < addrs / 4; i++) {
                    int ip = 0;
                    for (int j = 0; j < 4; j++)
                        ip = ip << 8 + ((int) m_options[offset++] & 0xff);

                    opt.add(new IPv4Address(ip));
                }
                options.add(opt);
            }
                break;

            case Option.CODE_ROUTE_RECORD: {
                LooseSourceRouteOption opt = new LooseSourceRouteOption();
                int addrs = ((int) m_options[offset] & 0xff) - 3;
                offset += 2;

                for (int i = 0; i < addrs / 4; i++) {
                    int ip = 0;
                    for (int j = 0; j < 4; j++)
                        ip = ip << 8 + ((int) m_options[offset++] & 0xff);

                    opt.add(new IPv4Address(ip));
                }
                options.add(opt);
            }
                break;

            default:
                throw new InstantiationException("Unsupported Option Type");

            } // end switch
        }

        return options;
    } // end method

    /**
     * Adds an option to the IP header.
     * 
     * @param opt
     *            The option to add to the header.
     * 
     */
    public void addOption(Option opt) {
        int origLen = 0;
        if (m_options == null) {
            int len = opt.bytesRequired();
            if ((len % 4) != 0)
                len = 4 - (len % 4);

            m_options = new byte[opt.bytesRequired()];
            int off = opt.writeBytes(m_options, 0);
            while (off < len)
                m_options[off++] = (byte) 0;
        } else {
            origLen = m_options.length;
            if (origLen + opt.bytesRequired() > 40)
                throw new IndexOutOfBoundsException("Option List is too long, must be less than 40 bytes");

            int len = origLen + opt.bytesRequired();
            if ((len % 4) != 0)
                len += 4 - (len % 4);

            byte[] ndata = new byte[len];
            System.arraycopy(m_options, 0, ndata, 0, origLen);

            int off = opt.writeBytes(ndata, origLen);
            while (off < len)
                ndata[off++] = (byte) 0;
        }

        m_hdrlen = (byte) ((20 + m_options.length) / 4);
    }

    /**
     * Stores the IP header as an array of bytes into the passed data buffer.
     * The IP header is written starting at the specified offset, and the new
     * offset is returned to the caller.
     * 
     * @param data
     *            The location to write the data
     * @param offset
     *            The offset to start storing information.
     * 
     * @return The new offset just beyond the last written byte.
     * 
     */
    public int writeBytes(byte[] data, int offset) {
        data[offset++] = (byte) ((m_version << 4) | (m_hdrlen & 0xf));
        data[offset++] = (byte) m_tos;
        data[offset++] = (byte) ((m_length >> 8) & 0xff);
        data[offset++] = (byte) (m_length & 0xff);
        data[offset++] = (byte) ((m_identity >> 8) & 0xff);
        data[offset++] = (byte) (m_identity & 0xff);
        data[offset++] = (byte) ((m_flags << 5) | ((m_fragOffset >> 8) & 0xff));
        data[offset++] = (byte) (m_fragOffset & 0xff);
        data[offset++] = (byte) m_ttl;
        data[offset++] = (byte) m_protocol;
        data[offset++] = (byte) ((m_checksum >> 8) & 0xff);
        data[offset++] = (byte) (m_checksum & 0xff);
        data[offset++] = (byte) ((m_srcAddr >> 24) & 0xff);
        data[offset++] = (byte) ((m_srcAddr >> 16) & 0xff);
        data[offset++] = (byte) ((m_srcAddr >> 8) & 0xff);
        data[offset++] = (byte) (m_srcAddr & 0xff);
        data[offset++] = (byte) ((m_dstAddr >> 24) & 0xff);
        data[offset++] = (byte) ((m_dstAddr >> 16) & 0xff);
        data[offset++] = (byte) ((m_dstAddr >> 8) & 0xff);
        data[offset++] = (byte) (m_dstAddr & 0xff);
        System.arraycopy(m_options, 0, data, offset, m_options.length);
        offset += m_options.length;
        return offset;
    }

    /**
     * Converts the passed 32-bit IPv4 address to a dotted decimal IP address
     * string.
     * 
     * @param ipv4Addr
     *            The 32-bit address
     * 
     * @return The dotted decimal address in the format "xxx.xxx.xxx.xxx" where
     *         0 <= xxx < 256
     * 
     */
    public static String addressToString(int ipv4Addr) {
        StringBuffer buf = new StringBuffer();
        buf.append((ipv4Addr >> 24) & 0xff);
        buf.append('.');
        buf.append((ipv4Addr >> 16) & 0xff);
        buf.append('.');
        buf.append((ipv4Addr >> 8) & 0xff);
        buf.append('.');
        buf.append(ipv4Addr & 0xff);

        return buf.toString();
    }

    /**
     * Converts the passed IPv4 address buffer to a dotted decimal IP address
     * string.
     * 
     * @param buf
     *            The 4 byte buffer
     * 
     * @return The dotted decimal address in the format "xxx.xxx.xxx.xxx" where
     *         0 <= xxx < 256
     * 
     * @exception IllegalArgumentException
     *                Thrown if the buffer is not exactly 4 bytes in length.
     */
    public static String addressToString(byte[] buf) {
        if (buf.length != 4)
            throw new IllegalArgumentException("IPv4 Address must be 4-bytes in length");

        int a = (buf[0] < 0) ? (int) buf[0] + 256 : (int) buf[0];
        int b = (buf[1] < 0) ? (int) buf[1] + 256 : (int) buf[1];
        int c = (buf[2] < 0) ? (int) buf[2] + 256 : (int) buf[2];
        int d = (buf[3] < 0) ? (int) buf[3] + 256 : (int) buf[3];

        StringBuffer sbuf = new StringBuffer();
        sbuf.append(a).append('.').append(b).append('.').append(c).append('.').append(d);

        return sbuf.toString();
    }

}
