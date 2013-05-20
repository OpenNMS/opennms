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

import org.opennms.protocols.snmp.asn1.ASN1;
import org.opennms.protocols.snmp.asn1.AsnEncoder;
import org.opennms.protocols.snmp.asn1.AsnEncodingException;

/**
 * <P>
 * The trap session is used to send and receives SNMPv1 & v2 trap messages. The
 * messages are received on the configured port, or the default(162) port and
 * then decoded using the set ASN.1 codec. When messages are sent they are
 * encoded using the passed SnmpParameters object that is part of the SnmpPeer
 * object.
 * </P>
 * 
 * <P>
 * A trap message handler must be bound to the session in order to send or
 * receive messages.
 * </P>
 * 
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author Sowmya
 * @version 1.1.1.1 2001/11/11 17:27:22
 * 
 * @see SnmpTrapHandler
 */
public final class SnmpTrapSession extends Object {
    /**
     * <P>
     * Defines a error due to a thown exception. When the snmpTrapSessionError
     * method is invoked in the trap handler, the exception object is passed as
     * the ref parameter.
     * </P>
     * 
     * @see SnmpTrapHandler#snmpTrapSessionError
     * 
     */
    public final static int ERROR_EXCEPTION = -1;

    /**
     * <P>
     * Defines an error condition with an invalid PDU. For the moment this is
     * not actually used, but reserved for future use. When the session trap
     * handler error method is invoke the pdu in error should be passed as the
     * ref parameters
     * 
     * @see SnmpTrapHandler#snmpTrapSessionError
     */
    public final static int ERROR_INVALID_PDU = -2;

    /**
     * This is the default port where traps should be sent and received as
     * defined by the RFC.
     * 
     */
    public final static int DEFAULT_PORT = 162;

    /**
     * The default SNMP trap callback handler. If this is not set and it is
     * needed then an SnmpHandlerNotDefinedException is thrown.
     * 
     */
    private SnmpPortal m_portal;

    /**
     * ASN.1 codec used to encode/decode SNMP traps that are sent and received
     * by this session.
     */
    private AsnEncoder m_encoder;

    /**
     * The public trap handler that process received traps.
     * 
     */
    private SnmpTrapHandler m_handler;

    /**
     * <P>
     * The internal trap handler class is designed to receive information from
     * the enclosed SnmpPortal class. The information is the processed and
     * forwarded when appropiate to the SnmpTrapHandler registered with the
     * session.
     * </P>
     * 
     */
    private class TrapHandler implements SnmpPacketHandler {
        /**
         * Who to pass as the session parameter
         */
        private SnmpTrapSession m_forWhom;

        /**
         * <P>
         * Creates a in internal trap handler to be the intermediary for the
         * interface between the SnmpPortal and the TrapSession.
         * </P>
         * 
         * @param sess
         *            The trap session reference.
         * 
         */
        public TrapHandler(SnmpTrapSession sess) {
            m_forWhom = sess;
        }

        /**
         * <P>
         * Processes the default V1 & V2 messages.
         * </P>
         * 
         * @param agent
         *            The sending agent
         * @param port
         *            The remote port.
         * @param version
         *            The SNMP Version of the message.
         * @param community
         *            The community string from the message.
         * @param pduType
         *            The type of pdu
         * @param pdu
         *            The actual pdu
         * 
         * @exception SnmpPduEncodingException
         *                Thrown if the pdu fails to decode.
         */
        @Override
        public void processSnmpMessage(InetAddress agent, int port, SnmpInt32 version, SnmpOctetString community, int pduType, SnmpPduPacket pdu) throws SnmpPduEncodingException {
            if (version.getValue() != SnmpSMI.SNMPV2 && pduType != SnmpPduPacket.V2TRAP)
                return;

            try {
                m_handler.snmpReceivedTrap(m_forWhom, agent, port, community, pdu);
            } catch (Exception e) {
                // discard
            }
        }

        /**
         * <P>
         * Processes V1 trap messages.
         * </P>
         * 
         * @param agent
         *            The sending agent
         * @param port
         *            The remote port.
         * @param community
         *            The community string from the message.
         * @param pdu
         *            The actual pdu
         * 
         * @exception SnmpPduEncodingException
         *                Thrown if the pdu fails to decode.
         */
        @Override
        public void processSnmpTrap(InetAddress agent, int port, SnmpOctetString community, SnmpPduTrap pdu) throws SnmpPduEncodingException {
            try {
                m_handler.snmpReceivedTrap(m_forWhom, agent, port, community, pdu);
            } catch (Exception e) {
                // discard
            }
        }

        /**
         * <P>
         * Invoked when bad datagrams are received.
         * </P>
         * 
         * @param p
         *            The datagram packet in question.
         * 
         */
        @Override
        public void processBadDatagram(DatagramPacket p) {
            // do nothing - discard?
        }

        /**
         * <P>
         * Invoked when an exception occurs in the session.
         * </P>
         * 
         * @param e
         *            The exception.
         */
        @Override
        public void processException(Exception e) {
            try {
                m_handler.snmpTrapSessionError(m_forWhom, ERROR_EXCEPTION, e);
            } catch (Exception e1) {
                // discard
            }
        }
    }

    /**
     * Used to disallow the default constructor.
     * 
     * @exception java.lang.UnsupportedOperationException
     *                Thrown if the constructor is called.
     * 
     */
    @SuppressWarnings("unused")
    private SnmpTrapSession() throws java.lang.UnsupportedOperationException {
        throw new java.lang.UnsupportedOperationException("Illegal constructor call");
    }

    /**
     * The default SnmpTrapSession constructor.
     * 
     * @param handler
     *            The handler associated for message processing.
     * 
     * @exception java.net.SocketException
     *                If thrown it is from the creation of a DatagramSocket.
     * @exception java.lang.SecurityException
     *                Thrown if the security manager disallows the creation of
     *                the handler.
     */
    public SnmpTrapSession(final SnmpTrapHandler handler) throws SocketException {
        m_encoder = (new SnmpParameters()).getEncoder();
        m_handler = handler;
        m_portal = new SnmpPortal(new TrapHandler(this), m_encoder, DEFAULT_PORT);
    }

    /**
     * The default SnmpTrapSession constructor that takes a packet handler as
     * parameter. Also changes the default port to listen on
     * 
     * @exception java.net.SocketException
     *                If thrown it is from the creation of a DatagramSocket.
     */
    public SnmpTrapSession(final SnmpTrapHandler handler, final int port) throws SocketException {
        m_encoder = (new SnmpParameters()).getEncoder();
        m_handler = handler;
        m_portal = new SnmpPortal(new TrapHandler(this), m_encoder, port);
    }

    public SnmpTrapSession(final SnmpTrapHandler handler, final InetAddress address, final int snmpTrapPort) throws SocketException {
        m_encoder = (new SnmpParameters()).getEncoder();
        m_handler = handler;
        m_portal = new SnmpPortal(new TrapHandler(this), m_encoder, address, snmpTrapPort);
	}

	/**
     * Returns the trap handler for this trap session.
     * 
     * @return The SnmpTrapHandler
     */
    public SnmpTrapHandler getHandler() {
        return m_handler;
    }

    /**
     * Sets the trap handler for the session.
     * 
     * @param hdl
     *            The new packet handler
     * 
     */
    public void setHandler(SnmpTrapHandler hdl) {
        m_handler = hdl;
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
     * Returns true if the <CODE>close</CODE> method has been called. The
     * session cannot be used to send request after <CODE>close</CODE> has
     * been executed.
     * 
     */
    public boolean isClosed() {
        return m_portal.isClosed();
    }

    /**
     * Used to close the session. Once called the session should be considered
     * invalid and unusable.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the session was already closed.
     */
    public void close() {
        if (m_portal.isClosed())
            throw new IllegalStateException("Illegal operation, the session is already closed");

        m_portal.close();
    }

    /**
     * Transmits the specified SnmpPduTrap to the SnmpPeer defined The
     * SnmpPduTrap is encoded using the peer AsnEncoder, as defined by the
     * SnmpParameters. Once the packet is encoded it is transmitted to the agent
     * defined by SnmpPeer. If an error occurs an appropiate exception is
     * generated.
     * 
     * @param peer
     *            The remote peer to send to.
     * @param trap
     *            The SnmpPduTrap to transmit
     * 
     * @exception SnmpPduEncodingException
     *                Thrown if an encoding exception occurs at the session
     *                level
     * @exception org.opennms.protocols.snmp.asn1.AsnEncodingException
     *                Thrown if an encoding exception occurs in the AsnEncoder
     *                object.
     * @exception java.io.IOException
     *                Thrown if an error occurs sending the encoded datagram
     * @exception java.lang.IllegalStateException
     *                Thrown if the session has been closed.
     * 
     * @see SnmpRequest
     * @see SnmpParameters
     * @see SnmpPeer
     * 
     */
    public void send(SnmpPeer peer, SnmpPduTrap trap) throws SnmpPduEncodingException, AsnEncodingException, java.io.IOException {
        if (m_portal.isClosed())
            throw new IllegalStateException("Illegal operation, the session has been closed");

        SnmpParameters parms = peer.getParameters();

        if (parms.getVersion() != SnmpSMI.SNMPV1) {
            throw new SnmpPduEncodingException("Cannot send pdu, invalid SNMP version");
        }

        //
        // Get the encoder and start
        // the encoding process
        //
        AsnEncoder encoder = parms.getEncoder();

        //
        // get a suitable buffer (16k)
        //
        int offset = 0;
        byte[] buf = new byte[16 * 1024];

        //
        // encode the SNMP version
        //
        SnmpInt32 version = new SnmpInt32(parms.getVersion());
        offset = version.encodeASN(buf, offset, encoder);

        //
        // get the correct community string. The
        // SET command uses the write community, all
        // others use the read community
        //
        SnmpOctetString community = new SnmpOctetString(parms.getReadCommunity().getBytes());

        //
        // encode the community strings
        //
        offset = community.encodeASN(buf, offset, encoder);

        //
        // Encode the actual trap
        //
        offset = trap.encodeASN(buf, offset, encoder);

        //
        // build the header, don't forget to mark the
        // pivot point
        //
        int pivot = offset;
        offset = encoder.buildHeader(buf, offset, (byte) (ASN1.SEQUENCE | ASN1.CONSTRUCTOR), pivot);

        //
        // rotate the buffer around the pivot point
        //
        SnmpUtil.rotate(buf, 0, pivot, offset);

        //
        // transmit the datagram
        //
        m_portal.send(peer, buf, offset);
    }

    /**
     * Transmits the specified SnmpRequest to the SnmpPeer defined. First the
     * SnmpPdu contained within the request is encoded using the peer
     * AsnEncoder, as defined by the SnmpParameters. Once the packet is encoded
     * it is transmitted to the agent defined by SnmpPeer. If an error occurs an
     * appropiate exception is generated.
     * 
     * @param peer
     *            The remote peer to send to.
     * @param pdu
     *            The pdu to transmit
     * 
     * @exception SnmpPduEncodingException
     *                Thrown if an encoding exception occurs at the session
     *                level
     * @exception org.opennms.protocols.snmp.asn1.AsnEncodingException
     *                Thrown if an encoding exception occurs in the AsnEncoder
     *                object.
     * @exception java.io.IOException
     *                Thrown if an error occurs sending the encoded datagram
     * @exception java.lang.IllegalStateException
     *                Thrown if the session has been closed.
     * 
     * @see SnmpRequest
     * @see SnmpParameters
     * @see SnmpPeer
     * 
     */
    public void send(SnmpPeer peer, SnmpPduPacket pdu) throws SnmpPduEncodingException, AsnEncodingException, java.io.IOException {
        if (m_portal.isClosed())
            throw new IllegalStateException("Illegal operation, the session has been closed");

        //
        // break down the pieces into usable variables
        //
        SnmpParameters parms = peer.getParameters();

        //
        // verify that for a SNMPV1 session that no
        // SNMPV2 packets are transmitted!
        //
        switch (pdu.getCommand()) {
        case SnmpPduPacket.V2TRAP:
            if (parms.getVersion() < SnmpSMI.SNMPV2) {
                throw new SnmpPduEncodingException("Cannot send pdu, invalid SNMP version");
            }
            break;

        default:
            throw new SnmpPduEncodingException("Invalid pdu, not a trap");
        }

        //
        // Get the encoder and start
        // the encoding process
        //
        AsnEncoder encoder = parms.getEncoder();

        //
        // get a suitable buffer (16k)
        //
        int offset = 0;
        byte[] buf = new byte[16 * 1024];

        //
        // encode the SNMP version
        //
        SnmpInt32 version = new SnmpInt32(parms.getVersion());
        offset = version.encodeASN(buf, offset, encoder);

        //
        // get the correct community string. The
        // SET command uses the write community, all
        // others use the read community
        //
        SnmpOctetString community = new SnmpOctetString(parms.getReadCommunity().getBytes());

        //
        // encode the community strings
        //
        offset = community.encodeASN(buf, offset, encoder);
        offset = pdu.encodeASN(buf, offset, encoder);

        //
        // build the header, don't forget to mark the
        // pivot point
        //
        int pivot = offset;
        offset = encoder.buildHeader(buf, offset, (byte) (ASN1.SEQUENCE | ASN1.CONSTRUCTOR), pivot);

        //
        // rotate the buffer around the pivot point
        //
        SnmpUtil.rotate(buf, 0, pivot, offset);

        //
        // transmit the datagram
        //
        m_portal.send(peer, buf, offset);
    }

} // end of SnmpTrapSession class
