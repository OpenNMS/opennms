/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors.support;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <PRE>
 *
 * The DNSAddressRequest holds a DNS request to lookup the IP address of a host -
 * provides for transmitting and receiving the response for this lookup.
 *
 * NOTES: A DNS request and response has the following fileds header questions
 * answers authorities additional information
 *
 * The header has the following format: id - unique id sent by the client and
 * returned by the server in its response 16 bits of flags -
 * Query(0)/response(1) flag opcode - that has type of query AA - set if the
 * response is an authoritative answer TC - set if response is truncated RD -
 * set if recursion is desired RA - set if recursion is available Z - reserved
 * bits RCODE - response code
 *
 * This class checks only for the received response to have the answer(which
 * will hold the IP address) - ignores the authorities and additional info
 *
 * </PRE>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class DNSAddressRequest {
    /**
     * <P>
     * Defines the class internet in the domain name system.
     * </P>
     */
    public static final int CLASS_IN = 1; // internet

    /**
     * <P>
     * Defines the address type.
     * </P>
     */
    public static final int TYPE_ADDR = 1; // address

    /**
     * <P>
     * The offset of the query bit in the header.
     * </P>
     */
    public static final int SHIFT_QUERY = 15;

    /**
     * <P>
     * The offset of the opcode bits in the header.
     * </P>
     */
    public static final int SHIFT_OPCODE = 11;

    /**
     * <P>
     * The offset of the authoritative bit in the header.
     * </P>
     */
    public static final int SHIFT_AUTHORITATIVE = 10;

    /**
     * <P>
     * The offset of the truncated bit in the header.
     * </P>
     */
    public static final int SHIFT_TRUNCATED = 9;

    /**
     * <P>
     * The offset of the recurse req bit in the header.
     * </P>
     */
    public static final int SHIFT_RECURSE_PLEASE = 8;

    /**
     * <P>
     * The offset of the requrse avail bit in the header.
     * </P>
     */
    public static final int SHIFT_RECURSE_AVAILABLE = 7;

    /**
     * <P>
     * The offset of the reserved bits in the header.
     * </P>
     */
    public static final int SHIFT_RESERVED = 4;

    /**
     * <P>
     * The offset of the response code bits in the header.
     * </P>
     */
    public static final int SHIFT_RESPONSE_CODE = 0;

    /**
     * <P>
     * The op code for a query in the header.
     * </P>
     */
    public static final int OPCODE_QUERY = 0;

    /**
     * <P>
     * The host to request information from. This would be the nameserver if it
     * supports DNS.
     * </P>
     */
    public String m_reqHost;

    /**
     * <P>
     * The id used to seralize the request. This allows the client (us) and the
     * server (host) to match exchanges.
     * </P>
     */
    public int m_reqID;

    /**
     * <P>
     * True if the answer is authoratitve.
     * </P>
     */
    public boolean m_authoritative;

    /**
     * <P>
     * True if the message is truncated.
     * </P>
     */
    public boolean m_truncated;

    /**
     * <P>
     * True if the message is recursive.
     */
    public boolean m_recursive;

    /**
     * <P>
     * The list of answers.
     * </P>
     */
    public List<DNSAddressRR> m_answers;

    /**
     * <P>
     * The global id, used to get the request id.
     * </P>
     */
    private static int globalID = 1;
    
    /**
     * <P>
     * The list of response codes to be considered fatal
     * </P>
     */
    private List<Integer> m_fatalResponseCodes;

    /**
     * <P>
     * Decodes the integer to get the flags - refer header for more info on the
     * flags.
     * </P>
     */
    private void decodeFlags(int flags) throws IOException {
        //
        // check the response flag
        //
        boolean isResponse = ((flags >> SHIFT_QUERY) & 1) != 0;
        if (!isResponse)
            throw new IOException("Response flag not set");

        //
        // check if error free
        //
        int code = (flags >> SHIFT_RESPONSE_CODE) & 15;

        if (isResponseCodeFatal(code)) {
            throw new IOException(codeName(code) + " (" + code + ")");
        }

        //
        // set the members of the instance.
        //
        m_authoritative = ((flags >> SHIFT_AUTHORITATIVE) & 1) != 0;
        m_truncated = ((flags >> SHIFT_TRUNCATED) & 1) != 0;
        m_recursive = ((flags >> SHIFT_RECURSE_AVAILABLE) & 1) != 0;

    }

    /**
     * <P>
     * Constructs a DNSAddressRequest for the hostname passed. The host string
     * that is passed to the address string should be a hostname in "x.y.z"
     * where x, y, and z are strings. This is not suppose to be a dotted decimal
     * address.
     * </P>
     *
     * @param host
     *            hostname for which address is to be constructed
     */
    public DNSAddressRequest(String host) {
        //
        // Imitate the original behavior of only a ServFail being fatal
        //
        m_fatalResponseCodes = new ArrayList<>();
        m_fatalResponseCodes.add(2);
        
        //
        // Split the host into its component
        // parts.
        //
        StringTokenizer labels = new StringTokenizer(host, ".");
        while (labels.hasMoreTokens()) {
            //
            // if any section is longer than
            // 63 characters then it's illegal
            //
            if (labels.nextToken().length() > 63)
                throw new IllegalArgumentException("Invalid hostname: " + host);
        }

        //
        // The requested host
        //
        m_reqHost = host;

        //
        // Synchronize on the class, not
        // the instance.
        //
        synchronized (DNSAddressRequest.class) {
            m_reqID = globalID % 65536;
            globalID = m_reqID + 1; // prevents negative numbers.
        }

        m_answers = new ArrayList<>();
    }

    /**
     * <P>
     * Builds the address request.
     * </P>
     *
     * @return A byte array containing the request.
     * @throws java.io.IOException if any.
     */
    public byte[] buildRequest() throws IOException {
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteArrayOut);

        dataOut.writeShort(m_reqID);
        dataOut.writeShort((0 << SHIFT_QUERY) | (OPCODE_QUERY << SHIFT_OPCODE) | (1 << SHIFT_RECURSE_PLEASE));

        dataOut.writeShort(1); // # queries
        dataOut.writeShort(0); // # answers
        dataOut.writeShort(0); // # authorities
        dataOut.writeShort(0); // # additional

        StringTokenizer labels = new StringTokenizer(m_reqHost, ".");
        while (labels.hasMoreTokens()) {
            String label = labels.nextToken();
            dataOut.writeByte(label.length());
            dataOut.writeBytes(label);
        }

        dataOut.writeByte(0);
        dataOut.writeShort(TYPE_ADDR);
        dataOut.writeShort(CLASS_IN);

        return byteArrayOut.toByteArray();
    }

    /**
     * <P>
     * Extracts the response from the bytearray.
     * </P>
     *
     * @param data
     *            The byte array containing the response.
     * @param length
     *            The length of the byte array.
     * @exception IOException
     *                Thrown if there is an error while reading the received
     *                packet
     * @throws java.io.IOException if any.
     */
    public void receiveResponse(byte[] data, int length) throws IOException {
        /*
         * Decode the input stream.
         */
        DNSInputStream dnsIn = new DNSInputStream(data, 0, length);
        int id = dnsIn.readShort();
        if (id != m_reqID)
            throw new IOException("ID does not match request");

        //
        // read in the flags
        //
        int flags = dnsIn.readShort();
        decodeFlags(flags);

        int numQueries = dnsIn.readShort();
        int numAnswers = dnsIn.readShort();
        @SuppressWarnings("unused")
        int numAuthorities = dnsIn.readShort();
        @SuppressWarnings("unused")
        int numAdditional = dnsIn.readShort();

        while (numQueries-- > 0) {
            //
            // discard questions
            //
            @SuppressWarnings("unused")
            String rname = dnsIn.readDomainName();
            @SuppressWarnings("unused")
            int rtype = dnsIn.readShort();
            @SuppressWarnings("unused")
            int rclass = dnsIn.readShort();
        }

        try {
            while (numAnswers-- > 0)
                m_answers.add(dnsIn.readRR());

            // ignore the authorities and additional information
            /**
             * while (numAuthorities -- > 0) dnsIn.readRR (); while
             * (numAdditional -- > 0) dnsIn.readRR ();
             */

        } catch (IOException ex) {
            if (!m_truncated)
                throw ex;
        }
    }

    /**
     * <P>
     * This method only goes so far as to decode the flags in the response byte
     * array to verify that a DNS server sent the response.
     * </P>
     *
     * <P>
     * NOTE: This is really a hack to get around the fact that the
     * receiveResponse() method is not robust enough to handle all possible DNS
     * server responses.
     *
     * @param data
     *            The byte array containing the response.
     * @param length
     *            The length of the byte array.
     * @exception IOException
     *                Thrown if there is an error while reading the received
     *                packet
     * @throws java.io.IOException if any.
     */
    public void verifyResponse(byte[] data, int length) throws IOException {
        /*
         * Decode the input stream.
         */
        DNSInputStream dnsIn = new DNSInputStream(data, 0, length);
        int id = dnsIn.readShort();
        if (id != m_reqID) {
            dnsIn.close();
            throw new IOException("ID in received packet (" + id + ") does not match ID from request (" + m_reqID + ")");
        }

        //
        // read in the flags
        //
        int flags = dnsIn.readShort();
        decodeFlags(flags);
        dnsIn.close();
    }

    /**
     * <P>
     * Return an enumeration of the received answers.
     * </P>
     *
     * @return The list of received answers.
     */
    public List<DNSAddressRR> getAnswers() {
        return m_answers;
    }

    /**
     * <P>
     * The request id for this particular instance.
     * </P>
     *
     * @return a int.
     */
    public int getRequestID() {
        return m_reqID;
    }

    /**
     * <P>
     * The hostname that will be request from the DNS box.
     * </P>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHost() {
        return m_reqHost;
    }

    /**
     * <P>
     * Returns true if the answer is truncated.
     * </P>
     *
     * @return a boolean.
     */
    public boolean isTruncated() {
        return m_truncated;
    }

    /**
     * <P>
     * Returns true if the answer is recursive.
     * </P>
     *
     * @return a boolean.
     */
    public boolean isRecursive() {
        return m_recursive;
    }

    /**
     * <P>
     * Returns true if the answer is authoritative.
     * </P>
     *
     * @return a boolean.
     */
    public boolean isAuthoritative() {
        return m_authoritative;
    }

    /**
     * <P>
     * Returns the code string for the error code received.
     * </P>
     *
     * @param code
     *            The error code.
     * @return The error string corresponding to the error code
     */
    public static String codeName(int code) {
        String[] codeNames = { "Format Error", "Server Failure", "Non-Existent Domain", "Not Implemented", "Query Refused" };

        return ((code >= 1) && (code <= 5)) ? codeNames[code - 1] : "Unknown error";
    }
    
    /**
     * <p>getFatalResponseCodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Integer> getFatalResponseCodes() {
        return m_fatalResponseCodes;
    }
    
    /**
     * <p>setFatalResponseCodes</p>
     *
     * @param codes a {@link java.util.List} object.
     */
    public void setFatalResponseCodes(List<Integer> codes) {
        m_fatalResponseCodes = codes;
    }
    
    private boolean isResponseCodeFatal(int code) {
        if (m_fatalResponseCodes.contains(code))
            return true;
        return false;
    }

}
