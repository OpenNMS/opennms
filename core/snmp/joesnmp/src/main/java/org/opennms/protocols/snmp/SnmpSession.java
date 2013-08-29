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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.ListIterator;

import org.opennms.protocols.snmp.asn1.ASN1;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * <P>
 * The SnmpSession is the main connection between the SNMP manager and the SNMP
 * Agent. All the request flow through this class. To use the SnmpSession class
 * a SnmpHandler class must be defined to process any errors or responses
 * through the library.
 * </P>
 * 
 * <P>
 * Once the session is created the creator must call <EM>close()</EM> to
 * ensure an orderly release of threads and resources.
 * </P>
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1 2001/11/11 17:27:22
 * 
 * @see SnmpHandler
 * @see SnmpPacketHandler
 * @see SnmpPortal
 */
public class SnmpSession extends Object {
    /**
     * This is the command passed to the SnmpHandler if a timeout occurs. All
     * errors are less than zero.
     * 
     * @see SnmpHandler
     */
    public static final int ERROR_TIMEOUT = -1;

    /**
     * This is the command passed to the SnmpHandler if an IOException occurs
     * while attempting to transmit the request
     * 
     * @see SnmpHandler
     */
    public static final int ERROR_IOEXCEPTION = -2;

    /**
     * This is the command passed to the SnmpHandler if an encoding exception is
     * generated when attempting to send an SnmpPduRequest message
     * 
     * @see SnmpHandler
     */
    public static final int ERROR_ENCODING = -3;

    /**
     * Used to contain a list of outstanding request for the session. The list
     * should only contain SnmpRequest objects!
     */
    private LinkedList<SnmpRequest> m_requests;

    /**
     * The SNMP peer to whom this session will communicate with. The peer
     * contains the parameters also.
     * 
     * @see SnmpParameters
     */
    private SnmpPeer m_peer;

    /**
     * The timer object used to schedule the SnmpRequest timeouts. It is also
     * used to schedule the cleanup of the m_requests list.
     * 
     * @see org.opennms.protocols.snmp.SnmpSession.CleanupRequest
     * @see SnmpRequest
     */
    private SnmpTimer m_timer;

    /**
     * The default SNMP callback handler. If this is not set and it is needed
     * then an SnmpHandlerNotDefinedException is thrown.
     */
    private SnmpHandler m_defHandler;

    /**
     * ASN encoder
     */
    AsnEncoder m_encoder;

    //
    // thread related stuff
    //

    /**
     * Provides a synchronization point
     */
    private Object m_sync;

    /**
     * If the boolean variable is set then the destroy() method must have been
     * called
     */
    private boolean m_stopRun;

    /**
     * the receiver thread
     */
    private SnmpPortal m_portal;

    /**
     * Inner class SessionHandler implements the interface SnmpPacketHandler to
     * handle callbacks from the SnmpPortal
     */
    private class SessionHandler implements SnmpPacketHandler {
        @Override
        public void processSnmpMessage(InetAddress agent, int port, SnmpInt32 version, SnmpOctetString community, int pduType, SnmpPduPacket pdu) throws SnmpPduEncodingException {
            //
            // now find the request and
            // inform
            //
            boolean isExpired = false;
            SnmpRequest req = null;
            synchronized (m_requests) {
                //
                // ensures that we get the proper
                // state information
                //
                req = findRequest(pdu);
                if (req != null)
                    isExpired = req.m_expired;
            }

            if (isExpired == false) {
                int cmd = -1;
                if (req != null && req.m_pdu instanceof SnmpPduPacket)
                    cmd = ((SnmpPduPacket) req.m_pdu).getCommand();
                else {
                    cmd = pdu.getCommand();
                    pdu.setPeer(new SnmpPeer(agent, port));
                }

                switch (cmd) {
                case SnmpPduPacket.SET: {
                    String tst = new String(community.getString());
                    String wr = m_peer.getParameters().getWriteCommunity();
                    if (!tst.equals(wr)) {
                        throw new SnmpPduEncodingException("Invalid community string");
                    }
                }
                    break;

                case SnmpPduPacket.GET:
                case SnmpPduPacket.GETNEXT:
                case SnmpPduPacket.RESPONSE:
                case SnmpPduPacket.INFORM:
                case SnmpPduPacket.GETBULK:
                case SnmpPduPacket.REPORT: {
                    String tst = new String(community.getString());
                    String rd = m_peer.getParameters().getReadCommunity();
                    if (!tst.equals(rd)) {
                        throw new SnmpPduEncodingException("Invalid community string");
                    }
                }
                    break;

                default:
                    throw new SnmpPduEncodingException("Invalid PDU Type for session received");
                }
                if (req != null) {
                    req.m_expired = true; // mark it as expired
                    req.m_handler.snmpReceivedPdu(req.m_session, ((SnmpPduRequest) pdu).getCommand(), (SnmpPduRequest) pdu);
                } else {
                    if (m_defHandler != null)
                        m_defHandler.snmpReceivedPdu(null, cmd, pdu);
                }
            }
        }

        @Override
        public void processSnmpTrap(InetAddress agent, int port, SnmpOctetString community, SnmpPduTrap pdu) throws SnmpPduEncodingException {
            throw new SnmpPduEncodingException("Invalid PDU Type for session");
        }

        @Override
        public void processBadDatagram(DatagramPacket p) {
            // do nothing - discard?
        }

        @Override
        public void processException(Exception e) {
            // do nothing - discard?
        }
    }

    /**
     * This class is used to periodically cleanup the outstanding request that
     * have expired. The cleanup interval is nominally once every 5 to 10
     * seconds. It's used like the garbage collector for the m_requests list.
     * This is used in hopes of minimizing the contention for the request array
     * 
     */
    private class CleanupRequest implements Runnable {
        /**
         * Preforms the actual removal of the expired SnmpRequest elements.
         * 
         * @see SnmpRequest
         * 
         */
        @Override
        public void run() {
            synchronized (m_requests) {
                if (m_requests.size() > 0) {
                    ListIterator<SnmpRequest> iter = m_requests.listIterator(0);
                    while (iter.hasNext()) {
                        SnmpRequest req = iter.next();
                        if (req.m_expired)
                            iter.remove();
                    }
                }
            }

            //
            // reschedule
            //
            if (!m_stopRun)
                m_timer.schedule(this, 1000);
        }
    }

    /**
     * <P>
     * Encapsulates a byte array and the number of bytes of valid data in the
     * array. The length passed to the constructor is normally less then the
     * length of the encapsulated array.
     * </P>
     * 
     */
    private static class ByteArrayInfo {
        /**
         * The buffer
         */
        private byte[] m_buf;

        /**
         * The valid length of the buffer
         */
        private int m_length;

        /**
         * Builds an encapuslated array with the passed buffer and its <EM>
         * valid</EM> length set to <EM>length</EM>.
         * 
         * @param buf
         *            The buffer.
         * @param length
         *            The valid length of the buffer.
         */
        public ByteArrayInfo(byte[] buf, int length) {
            m_buf = buf;
            m_length = length;
        }

        /**
         * returns the encapsulated array
         */
        public byte[] array() {
            return m_buf;
        }

        /**
         * Returns the valid length of the encapsulate array
         */
        public int size() {
            return m_length;
        }
    }

    /**
     * <P>
     * This method is used to encode the passed protocol data unit and return
     * the encoding. The encoding is performed using the default encoder for the
     * session and limits the size to a 16K buffer.
     * </P>
     * 
     * @param pdu
     *            The pdu to encode
     * 
     * @return The encoded pdu request
     * 
     * @exception SnmpPduEncodingException
     *                Thrown if an encoding exception occurs at the session
     *                level
     * @exception org.opennms.protocols.snmp.asn1.AsnEncodingException
     *                Thrown if an encoding exception occurs in the AsnEncoder
     *                object.
     */
    private ByteArrayInfo encode(SnmpPduPacket pdu) throws SnmpPduEncodingException, AsnEncodingException {
        SnmpPeer peer = m_peer;
        SnmpParameters parms = peer.getParameters();

        //
        // Get the encoder and start
        // the encoding process
        //
        // get a suitable buffer (16k)
        //
        int offset = 0;
        byte[] buf = new byte[16 * 1024];

        //
        // encode the SNMP version
        //
        SnmpInt32 version = new SnmpInt32(parms.getVersion());
        offset = version.encodeASN(buf, offset, m_encoder);

        //
        // get the correct community string. The
        // SET command uses the write community, all
        // others use the read community
        //
        SnmpOctetString community;
        if (pdu.getCommand() == SnmpPduPacket.SET) {
            String wrComm = parms.getWriteCommunity();
            if (wrComm == null)
                throw new SnmpPduEncodingException("Requested SET but there is no write community");
            community = new SnmpOctetString(wrComm.getBytes());
        } else {
            community = new SnmpOctetString(parms.getReadCommunity().getBytes());
        }

        //
        // encode the community strings
        //
        offset = community.encodeASN(buf, offset, m_encoder);
        offset = pdu.encodeASN(buf, offset, m_encoder);

        //
        // build the header, don't forget to mark the
        // pivot point
        //
        int pivot = offset;
        offset = m_encoder.buildHeader(buf, offset, (byte) (ASN1.SEQUENCE | ASN1.CONSTRUCTOR), pivot);

        //
        // rotate the buffer around the pivot point
        //
        SnmpUtil.rotate(buf, 0, pivot, offset);
        return new ByteArrayInfo(buf, offset);
    }

    /**
     * <P>
     * This method is used to encode the passed protocol data unit and return
     * the encoding. The encoding is performed using the default encoder for the
     * session and limits the size to a 16K buffer.
     * </P>
     * 
     * @param pdu
     *            The pdu to encode
     * 
     * @return The encoded pdu request
     * 
     * @exception SnmpPduEncodingException
     *                Thrown if an encoding exception occurs at the session
     *                level
     * @exception org.opennms.protocols.snmp.asn1.AsnEncodingException
     *                Thrown if an encoding exception occurs in the AsnEncoder
     *                object.
     */
    private ByteArrayInfo encode(SnmpPduTrap pdu) throws SnmpPduEncodingException, AsnEncodingException {
        SnmpPeer peer = m_peer;
        SnmpParameters parms = peer.getParameters();
        //
        // get a suitable buffer (16k)
        //
        int offset = 0;
        byte[] buf = new byte[16 * 1024];

        //
        // encode the SNMP version
        //
        SnmpInt32 version = new SnmpInt32(parms.getVersion());
        offset = version.encodeASN(buf, offset, m_encoder);

        //
        // get the correct community string. The
        // SET command uses the write community, all
        // others use the read community
        //
        SnmpOctetString community = new SnmpOctetString(parms.getReadCommunity().getBytes());

        //
        // encode the community strings
        //
        offset = community.encodeASN(buf, offset, m_encoder);
        offset = pdu.encodeASN(buf, offset, m_encoder);

        //
        // build the header, don't forget to mark the
        // pivot point
        //
        int pivot = offset;
        offset = m_encoder.buildHeader(buf, offset, (byte) (ASN1.SEQUENCE | ASN1.CONSTRUCTOR), pivot);

        //
        // rotate the buffer around the pivot point
        //
        SnmpUtil.rotate(buf, 0, pivot, offset);
        return new ByteArrayInfo(buf, offset);
    }

    /**
     * Adds an outstanding request to the session. The access to the list is
     * synchronized on the actual list of request. This is done to allow
     * synchronization between addRequest(), removeRequest(), and findRequest()
     * 
     * @param req
     *            The request reference to add (not cloned)
     */
    void addRequest(SnmpRequest req) {
        synchronized (m_requests) {
            m_requests.addLast(req);
        }
    }

    /**
     * Removes an outstanding request from the session. The method uses the
     * Object.equals() to find a matching request. If the SnmpRequest object
     * does not override the equals() method the a "by reference" equality is
     * used.
     * 
     * @param req
     *            The request to remove. All matching request are removed.
     */
    void removeRequest(SnmpRequest req) {
        synchronized (m_requests) {
            if (m_requests.size() > 0) {
                ListIterator<SnmpRequest> iter = m_requests.listIterator(0);
                while (iter.hasNext()) {
                    SnmpRequest cmp = iter.next();
                    if (req.equals(cmp)) {
                        req.m_expired = true;
                        iter.remove();
                    }
                }
            }
        }
    }

    /**
     * Finds the first matching request in the list of outstanding request and
     * returns it to the caller. The matching is done by means using the SNMP
     * Protocol Data Unit's request id. If no match is found then a null is
     * returned
     * 
     * @param pdu
     *            The source pdu for the search.
     * 
     * @return Returns a SnmpRequest if a match is found. Otherwise a null is
     *         returned.
     * 
     */
    SnmpRequest findRequest(SnmpPduPacket pdu) {
        synchronized (m_requests) {
            if (m_requests.size() > 0) {
                ListIterator<SnmpRequest> iter = m_requests.listIterator(0);

                while (iter.hasNext()) {
                    SnmpRequest req = iter.next();
                    if (!req.m_expired && req.m_pdu instanceof SnmpPduPacket && ((SnmpPduPacket) req.m_pdu).getRequestId() == pdu.getRequestId()) {
                        return req;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the internal timer object for the SNMP Session.
     * 
     * @return The internal timer object
     * 
     */
    SnmpTimer getTimer() {
        return m_timer;
    }

    /**
     * Transmits the specified SnmpRequest to the SnmpPeer defined by the
     * session. First the SnmpPdu contained within the request is encoded using
     * the session AsnEncoder, as defined by the SnmpParameters. Once the packet
     * is encoded it is transmitted to the agent defined by SnmpPeer. If an
     * error occurs an appropiate exception is generated.
     * 
     * @param req
     *            The SnmpRequest to transmit
     * 
     * @exception SnmpPduEncodingException
     *                Thrown if an encoding exception occurs at the session
     *                level
     * @exception org.opennms.protocols.snmp.asn1.AsnEncodingException
     *                Thrown if an encoding exception occurs in the AsnEncoder
     *                object.
     * @exception java.io.IOException
     *                Thrown if an error occurs sending the encoded datagram
     * 
     * @see SnmpRequest
     * @see SnmpParameters
     * @see SnmpPeer
     * 
     */
    void transmit(SnmpRequest req) throws SnmpPduEncodingException, AsnEncodingException, java.io.IOException {
        //
        // break down the pieces into usable variables
        //
        SnmpPduPacket pdu = null;
        SnmpPduTrap trap = null;
        SnmpPeer peer = m_peer;
        SnmpParameters parms = peer.getParameters();

        if (req.m_pdu instanceof SnmpPduPacket)
            pdu = (SnmpPduPacket) req.m_pdu;

        if (req.m_pdu instanceof SnmpPduTrap)
            trap = (SnmpPduTrap) req.m_pdu;

        //
        // verify that for a SNMPV1 session that no
        // SNMPV2 packets are transmitted!
        //
        if (pdu != null) {
            switch (pdu.getCommand()) {
            case SnmpPduPacket.INFORM:
            case SnmpPduPacket.V2TRAP:
            case SnmpPduPacket.REPORT:
            case SnmpPduPacket.GETBULK:
                if (parms.getVersion() < SnmpSMI.SNMPV2) {
                    throw new SnmpPduEncodingException("Cannot send pdu, invalid SNMP version");
                }
            }

            //
            // transmit the datagram
            //
            ByteArrayInfo msg = encode(pdu);
            if (pdu.getPeer() == null)
                m_portal.send(m_peer, msg.array(), msg.size());
            else
                m_portal.send(pdu.getPeer(), msg.array(), msg.size());
        } else if (trap != null) {
            ByteArrayInfo msg = encode(trap);
            m_portal.send(m_peer, msg.array(), msg.size());
        } else {
            throw new SnmpPduEncodingException("Invalid PDU type passed to method");
        }
    }

    /**
     * The default SnmpSession constructor. The object is constructed with a
     * default SnmpPeer object.
     * 
     * @param peer
     *            The peer agent
     * 
     * @see SnmpPeer
     * 
     * @exception java.net.SocketException
     *                If thrown it is from the creation of a DatagramSocket.
     * 
     */
    public SnmpSession(InetAddress peer) throws SocketException {
        m_sync = new Object();
        m_requests = new LinkedList<SnmpRequest>();
        m_peer = new SnmpPeer(peer);
        m_timer = new SnmpTimer();
        m_defHandler = null;

        m_stopRun = false;

        m_encoder = (new SnmpParameters()).getEncoder();
        m_portal = new SnmpPortal(new SessionHandler(), m_encoder, 0);

        m_timer.schedule(new CleanupRequest(), 1000);
    }

    /**
     * Constructs the SnmpSession with the specific SnmpPeer.
     * 
     * @param peer
     *            The SnmpPeer used to configure this session
     * 
     * @see SnmpPeer
     * 
     * @exception java.net.SocketException
     *                If thrown it is from the creation of a DatagramSocket.
     * 
     */
    public SnmpSession(SnmpPeer peer) throws SocketException {
        m_requests = new LinkedList<SnmpRequest>();
        m_timer = new SnmpTimer();
        m_defHandler = null;

        m_sync = new Object();
        m_stopRun = false;

        m_encoder = peer.getParameters().getEncoder();
        m_portal = new SnmpPortal(new SessionHandler(), m_encoder,  peer.getServerPort());

        m_peer = peer;

        m_timer.schedule(new CleanupRequest(), 5000);
    }

    /**
     * Constructs the SnmpSession with the specific parameters. The parameters
     * are associated with the default SnmpPeer object.
     * 
     * @param peer
     *            The peer address for agent
     * @param params
     *            The SnmpParameters to configure with this session
     * 
     * @see SnmpPeer
     * @see SnmpParameters
     * 
     * @exception java.net.SocketException
     *                If thrown it is from the creation of a DatagramSocket.
     * 
     */
    public SnmpSession(InetAddress peer, SnmpParameters params) throws SocketException {
        this(peer);
        m_peer.setParameters(params);
    }

    /**
     * Gets the default SnmpHandler for the session. If the handler has never
     * been set via the setDefaultHandler() method then a null will be returned.
     * 
     * @return The default SnmpHandler, a null if one has never been registered.
     * 
     */
    public SnmpHandler getDefaultHandler() {
        return m_defHandler;
    }

    /**
     * Sets the default SnmpHandler.
     * 
     * @param hdl
     *            The new default handler
     * 
     */
    public void setDefaultHandler(SnmpHandler hdl) {
        m_defHandler = hdl;
    }

    /**
     * Gets the current peer object.
     * 
     * @return The current SnmpPeer object
     * 
     */
    public SnmpPeer getPeer() {
        return m_peer;
    }

    /**
     * Sets the passed SnmpPeer object to the one used for all new SNMP
     * communications. This includes any outstanding retries.
     * 
     * @param peer
     *            The SnmpPeer object for the sesison
     * 
     */
    public void setPeer(SnmpPeer peer) {
        m_peer = peer;
        setAsnEncoder(peer.getParameters().getEncoder());
    }

    /**
     * Returns the number of outstanding request for the agent. An outstanding
     * request is one that has no yet responded to the query.
     * 
     * @return The number of outstanding request
     * 
     * @throws java.lang.IllegalStateException
     *             Throw if the session has been closed.
     */
    public int getOutstandingCount() {
        //
        // check to ensure that the session is still open
        //
        synchronized (m_sync) {
            if (m_stopRun) // session has been closed!
                throw new IllegalStateException("illegal operation, the session has been closed");
        }

        synchronized (m_requests) {
            //
            // need to do cleanup in order
            // to make this happen!
            //
            if (m_requests.size() > 0) {
                ListIterator<SnmpRequest> iter = m_requests.listIterator();
                while (iter.hasNext()) {
                    SnmpRequest req = iter.next();
                    if (req.m_expired)
                        iter.remove();
                }
            }
            return m_requests.size();
        }
    }

    /**
     * Cancels the current outstanding reqeust as defined by the SnmpPduPacket's
     * requestId method.
     * 
     * @param requestId
     *            The request to cancel
     * 
     * @see SnmpPduPacket
     * 
     * @throws java.lang.IllegalStateException
     *             Throw if the session has been closed.
     */
    public void cancel(int requestId) {
        //
        // check to ensure that the session is still open
        //
        synchronized (m_sync) {
            if (m_stopRun) // session has been closed!
                throw new IllegalStateException("illegal operation, the session has been closed");
        }

        synchronized (m_requests) {
            if (m_requests.size() > 0) {
                ListIterator<SnmpRequest> iter = m_requests.listIterator();
                while (iter.hasNext()) {
                    //
                    // While the method owns the lock remove any expired
                    // request and any request with a matching request id
                    //
                    SnmpRequest req = iter.next();
                    if (req.m_expired || (req.m_pdu instanceof SnmpPduPacket && ((SnmpPduPacket) req.m_pdu).getRequestId() == requestId)) {
                        req.m_expired = true;
                        iter.remove();
                    }
                }
            }
        }
    }

    /**
     * Send the SNMP PDU to the remote agent and invokes the specified handler
     * when the packet is recieve. This is a non-blocking call.
     * 
     * @param pdu
     *            The pdu to encode and send
     * @param handler
     *            The handler object for this request
     * 
     * @return The request identifier for the newly sent PDU.
     * 
     * @exception SnmpHandlerNotDefinedException
     *                Thrown if the handler is null
     * @exception java.lang.IllegalStateException
     *                Thrown if the session has been closed.
     */
    public int send(SnmpPduPacket pdu, SnmpHandler handler) {
        if (handler == null)
            throw new SnmpHandlerNotDefinedException("No Handler Defined");

        //
        // check to ensure that the session is still open
        //
        synchronized (m_sync) {
            if (m_stopRun) // session has been closed!
                throw new IllegalStateException("illegal operation, the session has been closed");
        }

        SnmpRequest req = new SnmpRequest(this, pdu, handler);
        if (pdu.getCommand() != SnmpPduPacket.V2TRAP || pdu.getPeer() == null) // traps
                                                                                // and
                                                                                // responses
                                                                                // don't
                                                                                // get
                                                                                // answers!
            addRequest(req);

        req.run();
        if (req.m_expired == true) {
            return 0;
        }

        return ((SnmpPduPacket) req.m_pdu).getRequestId();
    }

    /**
     * Sends the SNMP PDU to the remote agent and uses the default SnmpHandler
     * to process the request. This is a non-blocking call
     * 
     * @param pdu
     *            The pdu to encode and send
     * 
     * @return The request identifier for the newly sent PDU.
     * 
     * @exception SnmpHandlerNotDefinedException
     *                Thrown if the handler is null
     * @exception java.lang.IllegalStateException
     *                Thrown if the session has been closed.
     */
    public int send(SnmpPduPacket pdu) {
        if (m_defHandler == null)
            throw new SnmpHandlerNotDefinedException("No Handler Defined");

        return send(pdu, m_defHandler);
    }

    /**
     * Send the SNMP PDU Trap to the remote agent. This is a non-blocking call.
     * 
     * @param pdu
     *            The pdu to encode and send
     * @param handler
     *            The handler object for this request
     * 
     * @return The request identifier for the newly sent PDU.
     * 
     * @exception SnmpHandlerNotDefinedException
     *                Thrown if the handler is null
     * @exception java.lang.IllegalStateException
     *                Thrown if the session has been closed.
     */
    public int send(SnmpPduTrap pdu, SnmpHandler handler) {
        if (handler == null)
            throw new SnmpHandlerNotDefinedException("No Handler Defined");

        //
        // check to ensure that the session is still open
        //
        synchronized (m_sync) {
            if (m_stopRun) // session has been closed!
                throw new IllegalStateException("illegal operation, the session has been closed");
        }

        SnmpRequest req = new SnmpRequest(this, pdu, handler);
        req.run();
        return 0;
    }

    /**
     * Sends the SNMP PDU Trap to the remote agent. This is a non-blocking call
     * 
     * @param pdu
     *            The pdu to encode and send
     * 
     * @return The request identifier for the newly sent PDU.
     * 
     * @exception SnmpHandlerNotDefinedException
     *                Thrown if the handler is null
     * @exception java.lang.IllegalStateException
     *                Thrown if the session has been closed.
     */
    public int send(SnmpPduTrap pdu) {
        if (m_defHandler == null)
            throw new SnmpHandlerNotDefinedException("No Handler Defined");

        return send(pdu, m_defHandler);
    }

    /**
     * Returns true if the <CODE>close</CODE> method has been called. The
     * session cannot be used to send request after <CODE>close</CODE> has
     * been executed.
     * 
     */
    public boolean isClosed() {
        synchronized (m_sync) {
            return m_stopRun;
        }
    }

    /**
     * Used to close the session. Once called the session should be considered
     * invalid and unusable.
     * 
     * @exception java.lang.IllegalStateException
     *                Thrown if the session has already been closed by another
     *                thread.
     */
    public void close() {
        synchronized (m_sync) {
            //
            // only allow close to be called once
            //
            if (m_stopRun)
                throw new IllegalStateException("The session is already closed");

            m_stopRun = true;
            m_timer.cancel();
        }
        m_portal.close();

        //
        // remove all items from the list
        //
        synchronized (m_requests) {
            m_requests.clear();
        }
    }

    /**
     * Sets the default encoder.
     * 
     * @param encoder
     *            The new encoder
     * 
     */
    public void setAsnEncoder(AsnEncoder encoder) {
        m_encoder = encoder;
        m_portal.setAsnEncoder(encoder);
    }

    /**
     * Gets the AsnEncoder for the session.
     * 
     * @return the AsnEncoder
     */
    public AsnEncoder getAsnEncoder() {
        return m_encoder;
    }

    /**
     * Allows library users to register new ASN.1 types with the SNMP library.
     * The object must support all methods of the SnmpSyntax interface. The
     * object is registered globally with the library and is visible to all
     * session after it is registered.
     * 
     * @param object
     *            The new SnmpSyntax object to register
     */
    public static void registerSyntaxObject(SnmpSyntax object) {
        SnmpUtil.registerSyntax(object);
    }

    public SnmpSyntax getNext(SnmpObjectId oid) {
        return getResult(SnmpPduPacket.GETNEXT, oid);
    }

    public SnmpSyntax[] getNext(SnmpObjectId[] oids) {
        return getResults(SnmpPduPacket.GETNEXT, oids);
    }
    
    public SnmpSyntax get(SnmpObjectId oid) {
        return getResult(SnmpPduPacket.GET, oid);
    }

    public SnmpSyntax set(SnmpObjectId oid, SnmpSyntax value) {
        return getResult(SnmpPduPacket.SET, oid, value);
    }

    public SnmpSyntax[] set(SnmpObjectId[] oids, SnmpSyntax[] values) {
        return getResults(SnmpPduPacket.SET, oids, values);
    }

    public SnmpSyntax[] get(SnmpObjectId[] oids) {
        return getResults(SnmpPduPacket.GET, oids);
    }
    
    public SnmpSyntax[] getBulk(int nonRepeaters, int maxReptitions, SnmpObjectId id) {
        return getBulk(nonRepeaters, maxReptitions, new SnmpObjectId[] { id });
    }
    
    public SnmpSyntax[] getBulk(int nonRepeaters, int maxRepititions, SnmpObjectId[] oids) {
        SnmpVarBind[] varbinds = createVarBinds(oids);
        SnmpPduPacket request = new SnmpPduBulk(nonRepeaters, maxRepititions, varbinds);
        return getResults(request);
    }

    private SnmpSyntax getResult(int requestType, SnmpObjectId oid) {
        SnmpSyntax[] result = getResults(requestType, new SnmpObjectId[] { oid });
        return (result == null || result.length <= 0 ? null : result[0]);
    }

    private SnmpSyntax getResult(int requestType, SnmpObjectId oid, SnmpSyntax value) {
        SnmpSyntax[] result = getResults(requestType, new SnmpObjectId[] { oid }, new SnmpSyntax[] { oid });
        return (result == null || result.length <= 0 ? null : result[0]);
    }

    private SnmpSyntax[] getResults(int requestType, SnmpObjectId[] oids) {
        SnmpVarBind[] varbinds = createVarBinds(oids);
        SnmpPduPacket request = new SnmpPduRequest(requestType, varbinds);
        return getResults(request);
    }

    private SnmpSyntax[] getResults(int requestType, SnmpObjectId[] oids, SnmpSyntax[] values) {
        SnmpVarBind[] varbinds = createVarBinds(oids,values);
        SnmpPduPacket request = new SnmpPduRequest(requestType, varbinds);
        return getResults(request);
    }

    private SnmpVarBind[] createVarBinds(SnmpObjectId[] oids) {
        SnmpVarBind[] varbinds = new SnmpVarBind[oids.length];
        for(int i = 0; i < oids.length; i++) {
            varbinds[i] = new SnmpVarBind(oids[i]);
        }
        return varbinds;
    }

    private SnmpVarBind[] createVarBinds(SnmpObjectId[] oids, SnmpSyntax[] values) {
        SnmpVarBind[] varbinds = new SnmpVarBind[oids.length];
        for(int i = 0; i < oids.length; i++) {
            varbinds[i] = new SnmpVarBind(oids[i],values[i]);
        }
        return varbinds;
    }

    private SnmpSyntax[] getResults(SnmpPduPacket request) {
        SnmpPduPacket response = getResponse(request);
        if (response == null)
            return null;
        SnmpSyntax[] vals = new SnmpSyntax[response.getLength()];
        for(int i = 0; i < response.getLength(); i++) {
            vals[i] = response.getVarBindAt(i).getValue();
        }
        return vals;
    }

    public SnmpPduPacket getResponse(SnmpPduPacket request) {
        SnmpResponseHandler handler = new SnmpResponseHandler();
        synchronized (handler) {
            send(request, handler);
            try {
                handler.wait((long) ((getPeer().getRetries() + 1) * getPeer().getTimeout()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        SnmpPduPacket response = handler.getResponse();
        return response;
    }





} // end of SnmpSession class
