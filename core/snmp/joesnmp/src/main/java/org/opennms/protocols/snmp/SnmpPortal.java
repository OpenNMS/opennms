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

import java.io.ByteArrayOutputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.LinkedList;

import org.opennms.protocols.snmp.asn1.ASN1;
import org.opennms.protocols.snmp.asn1.AsnDecodingException;
import org.opennms.protocols.snmp.asn1.AsnEncoder;

/**
 * Abstracts the communication related details from the SnmpSession and
 * SnmpTrapSession.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * 
 * @see SnmpSession
 * @see SnmpTrapSession
 * @see java.net.DatagramSocket
 */
public class SnmpPortal extends Object {
    /**
     * The packet handler that is used to process received SNMP packets and
     * invalid datagrams. The handler must also process any exceptions that
     * occurs in the receiving thread.
     * 
     */
    private SnmpPacketHandler m_handler;

    /**
     * The datagram socket used to send and receive SNMP messages.
     * 
     */
    private DatagramSocket m_comm;

    /**
     * the receiver thread that runs the inner class Receiver.
     */
    private Thread m_recvThread;

    /**
     * ASN.1 encoder used to decode the SNMP messages. If the decoded fails to
     * decode the specific messages the is should throw and appropiate ASN.1
     * exception
     * 
     */
    private AsnEncoder m_encoder;

    /**
     * When set the portal object's close method has been invoked. This is
     * needed since the internal receiver thread will block on the communication
     * channel. To "wake" the thread the close() method on the comm channel is
     * performed. This will cause an exception to be genereated in the receiver
     * thread. If the value of m_isClosing is true then the exception is
     * ignored.
     */
    private volatile boolean m_isClosing;

    /**
     * Set to true if it is necessary to set the socket timeout value via the
     * Socket.setSoTimeout() method in order to keep from blocking indefinitely
     * on a socket I/O call. This value is configurable at runtime via the
     * system property "org.opennms.joeSNMP.vmhacks.socketSoTimeoutRequired". If
     * this property is set to 'no', the bSocketSoTimeoutRequired variable will
     * be set to false and the SNMP trap socket timeout will not be set. If this
     * property is set to 'yes' or the property does not exist, the
     * bSocketSoTimeoutRequired variable will be set to true. and the socket
     * timeout will be set. Default value is true.
     */
    private boolean bSocketSoTimeoutRequired = true;

    /**
     * Identifies the system property that may be used to specify whether or not
     * a timeout value is set on the SNMP trap socket. Valid values are 'yes'
     * and 'no'.
     */
    private static final String PROP_SOCKET_TIMEOUT_REQUIRED = "org.opennms.joeSNMP.vmhacks.socketSoTimeoutRequired";

    /**
     * Identifies the system property that may be used to specify the number of
     * milliseconds to use for the socket timeout.
     */
    private static final String PROP_SOCKET_TIMEOUT_PERIOD = "org.opennms.joeSNMP.vmhacks.socketSoTimeoutPeriod";

    /**
     * Private constructor used to disallow the default constructor.
     * 
     * @exception java.lang.UnsupportedOperationException
     *                Always thrown!
     */
    @SuppressWarnings("unused")
    private SnmpPortal() throws java.lang.UnsupportedOperationException {
        throw new java.lang.UnsupportedOperationException("Illegal constructor call");
    }

    /**
     * The SnmpPortal constructor. The constructor is used to build a portal on
     * the specified port, and forward messages to the defined handler. All
     * messages are decoded using the encoder specified during construction.
     * 
     * @param handler
     *            The SNMP packet handler.
     * @param encoder
     *            The ASN.1 codec object.
     * @param port
     *            The port to send and receive datagram from.
     * 
     * @exception java.net.SocketException
     *                Thrown if an error occurs setting up the communication
     *                channel.
     * @exception java.lang.IllegalArgumentException
     *                Thrown if any of the parameters are null or invalid.
     * 
     */
    SnmpPortal(final SnmpPacketHandler handler, final AsnEncoder encoder, final int port) throws SocketException {
    	if (handler == null || encoder == null)
            throw new IllegalArgumentException("Invalid argument");

        m_handler = handler;

        if (port >= 0) {
            m_comm = new DatagramSocket(port);
        } else {
            m_comm = new DatagramSocket();
        }

        initializePortal(encoder);
    }

    SnmpPortal(final SnmpPacketHandler handler, final AsnEncoder encoder, final InetAddress address, final int port) throws SocketException {
    	if (handler == null || encoder == null)
            throw new IllegalArgumentException("Invalid argument");

        m_handler = handler;

        if (address == null) {
	        if (port >= 0) {
	            m_comm = new DatagramSocket(port);
	        } else {
	            m_comm = new DatagramSocket();
	        }
        } else {
        	m_comm = new DatagramSocket(port, address);
        }

        initializePortal(encoder);
    }
    
	public void initializePortal(final AsnEncoder encoder) throws SocketException {
		//
        // Determine whether or not it is necessary to use the
        // socket.setSoTimeout()
        // method to set the socket timeout value thereby mimic'ing non-blocking
        // socket I/O.
        // On platforms whose system close() is not preemptive it is necessary
        // to use the socket timeout
        // to keep from blocking indefinitely on any socket call that performs
        // I/O.
        //
        bSocketSoTimeoutRequired = true; // Default is to use set the socket
                                            // timeout
        String strSocketSoTimeoutRequired = System.getProperty(PROP_SOCKET_TIMEOUT_REQUIRED);
        String osName = System.getProperty("os.name");

        if (strSocketSoTimeoutRequired != null && strSocketSoTimeoutRequired.equals("no")) {
            bSocketSoTimeoutRequired = false;
        }

        if (bSocketSoTimeoutRequired == true) {
            String strSocketSoTimeoutPeriod = System.getProperty(PROP_SOCKET_TIMEOUT_PERIOD);
            int timeout = 3000; // Default socket timeout is 3 seconds
            if (strSocketSoTimeoutPeriod != null) {
                try {
                    timeout = Integer.parseInt(strSocketSoTimeoutPeriod);
                } catch (NumberFormatException e) {
                    timeout = 3000;
                }
            }
            m_comm.setSoTimeout(timeout);
        } else if (osName != null && osName.equalsIgnoreCase("linux")) {
            // we must force this issue because we do not know
            // what VM there running in. If there running in
            // Sun JDK 1.3.1 with J2SE_PREEMPTCLOSE set then
            // this is unnecessary. If there not running in 1.3.1
            // and the're on linux then THEY MUST have a timeout
            // set of it will hang a thread.
            //
            m_comm.setSoTimeout(100);
        }

        m_isClosing = false;

        m_recvThread = new Thread(new Receiver(), "SnmpPortal-" + m_comm.getPort());
        m_encoder = encoder;

        m_recvThread.start();
	}

	/**
     * Defines the inner class that monitors the datagram socket and receives
     * all the PDU responses. If an exception is generated then it is saved in
     * m_why and can be re-generated with a call to raise().
     * 
     */
    private class Receiver implements Runnable {
        /**
         * Called to setup the communications channel buffers. The method
         * attempts to set the received buffer size to 64k. If it fails then the
         * default buffer size is recovered. If the default buffer size cannot
         * be recovered then a zero is returned.
         * 
         * @return The communications channel receive buffer size. A zero is
         *         returned on error
         */
        private int setup() {
            int bufSz = 64 * 1024;

            //
            // set the receiver buffer
            //
            try {
                m_comm.setReceiveBufferSize(bufSz);
            } catch (SocketException err) {
                bufSz = 0;
            }

            if (bufSz == 0) {
                try {
                    bufSz = m_comm.getReceiveBufferSize();
                } catch (SocketException err) {
                    bufSz = 0;
                }
            }
            return bufSz;
        }

        /**
         * The run method is an infinite loop method that receives all datagrams
         * for the session. If an unrecoverable error occurs then the m_handler
         * is informed of the error
         * 
         * If a pdu is recovered from the channel then the associated handler is
         * invoked to process the pdu.
         * 
         * @see SnmpPacketHandler
         */
        @Override
        public void run() {
            final int bufSz = setup();
            if (bufSz == 0) {
                return;
            }

            final LinkedList<DatagramPacket> fastReceiverQ = new LinkedList<DatagramPacket>();
            final LinkedList<byte[]> usedBuffers = new LinkedList<byte[]>();
            Thread fastReceiver = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!m_isClosing && Thread.interrupted() == false) {
                        byte[] buf = null;
                        synchronized (usedBuffers) {
                            if (!usedBuffers.isEmpty())
                                buf = (byte[]) usedBuffers.removeFirst();
                        }

                        if (buf == null || buf.length != bufSz)
                            buf = new byte[bufSz];

                        try {
                            DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                            m_comm.receive(pkt);
                            synchronized (fastReceiverQ) {
                                fastReceiverQ.addLast(pkt);
                                fastReceiverQ.notify();
                            }
                        } catch (InterruptedIOException ioe) {
                            synchronized (usedBuffers) {
                                usedBuffers.addLast(buf);
                            }
                            continue;
                        } catch (Exception e) {
                            if (!m_isClosing) {
                                boolean handled = true;
                                try {
                                    Class<?> loggerC = Class.forName("org.opennms.core.utils.ThreadCategory");

                                    Class<?>[] methodParmList = { Class.class };
                                    Method loggerM = loggerC.getMethod("getInstance", methodParmList);

                                    Object[] parmList = { this.getClass() };
                                    Object loggerI = loggerM.invoke(null, parmList);

                                    methodParmList = new Class[] { Object.class, Throwable.class };
                                    Method infoM = loggerC.getMethod("info", methodParmList);

                                    parmList = new Object[] { "An unknown error occured decoding the packet", e };
                                    infoM.invoke(loggerI, parmList);
                                } catch (Throwable t) {
                                    handled = false;
                                }

                                if (!handled) {
                                    System.out.println(new Date() + " - Exception: " + e.getMessage());
                                }
                                m_handler.processException(e);
                            }
                        }
                    }
                }
            }, Thread.currentThread().getName() + "-FastReceiver");
            fastReceiver.start();

            //
            // get a buffer for the datagrams
            //
            while (!m_isClosing) {
                DatagramPacket pkt = null;
                try {
                    //
                    // reset the packet's length
                    //
                    synchronized (fastReceiverQ) {
                        while (fastReceiverQ.isEmpty() && !m_isClosing)
                            fastReceiverQ.wait(300);

                        if (m_isClosing)
                            continue;

                        pkt = (DatagramPacket) fastReceiverQ.removeFirst();
                    }
                    handlePkt(pkt);
                } catch (SnmpPduEncodingException err) {
                    boolean handled = true;
                    try {
                        Class<?> loggerC = Class.forName("org.opennms.core.utils.ThreadCategory");

                        Class<?>[] methodParmList = { Class.class };
                        Method loggerM = loggerC.getMethod("getInstance", methodParmList);

                        Object[] parmList = { this.getClass() };
                        Object loggerI = loggerM.invoke(null, parmList);

                        methodParmList = new Class[] { Object.class, Throwable.class };
                        Method infoM = loggerC.getMethod("info", methodParmList);

                        parmList = new Object[] { "An error occured decoding the protocol data unit", err };
                        infoM.invoke(loggerI, parmList);

                        methodParmList = new Class[0];
                        Method debugEnabledM = loggerC.getMethod("isDebugEnabled", methodParmList);

                        parmList = new Object[0];
                        Boolean isEnabled = (Boolean) debugEnabledM.invoke(loggerI, parmList);

                        if (isEnabled.booleanValue()) {
                            methodParmList = new Class[] { Object.class };
                            Method debugM = loggerC.getMethod("debug", methodParmList);

                            OutputStream ostream = new ByteArrayOutputStream();
                            SnmpUtil.dumpHex(new PrintStream(ostream), pkt.getData(), 0, pkt.getLength());

                            parmList = new Object[] { ostream };
                            debugM.invoke(loggerI, parmList);
                        }
                    } catch (Throwable t) {
                        handled = false;
                    }

                    if (!handled) {
                        System.out.println(new Date() + " - SnmpPortal.Receiver.run: SnmpPduEncodingException: " + err.getMessage());
                        SnmpUtil.dumpHex(System.out, pkt.getData(), 0, pkt.getLength());
                    }
                    m_handler.processBadDatagram(pkt);
                } catch (AsnDecodingException err) {
                    boolean handled = true;
                    try {
                        Class<?> loggerC = Class.forName("org.opennms.core.utils.ThreadCategory");

                        Class<?>[] methodParmList = { Class.class };
                        Method loggerM = loggerC.getMethod("getInstance", methodParmList);

                        Object[] parmList = { this.getClass() };
                        Object loggerI = loggerM.invoke(null, parmList);

                        methodParmList = new Class[] { Object.class, Throwable.class };
                        Method infoM = loggerC.getMethod("info", methodParmList);

                        parmList = new Object[] { "An ASN.1 error occured decoding the packet", err };
                        infoM.invoke(loggerI, parmList);

                        methodParmList = new Class[0];
                        Method debugEnabledM = loggerC.getMethod("isDebugEnabled", methodParmList);

                        parmList = new Object[0];
                        Boolean isEnabled = (Boolean) debugEnabledM.invoke(loggerI, parmList);

                        if (isEnabled.booleanValue()) {
                            methodParmList = new Class[] { Object.class };
                            Method debugM = loggerC.getMethod("debug", methodParmList);

                            OutputStream ostream = new ByteArrayOutputStream();
                            SnmpUtil.dumpHex(new PrintStream(ostream), pkt.getData(), 0, pkt.getLength());

                            parmList = new Object[] { ostream };
                            debugM.invoke(loggerI, parmList);
                        }
                    } catch (Throwable t) {
                        handled = false;
                    }

                    if (!handled) {
                        System.out.println(new Date() + " - SnmpPortal.Receiver.run: AsnEncodingException: " + err.getMessage());
                        SnmpUtil.dumpHex(System.out, pkt.getData(), 0, pkt.getLength());
                    }
                    m_handler.processBadDatagram(pkt);
                } catch (Exception e) {
                    if (!m_isClosing) {
                        boolean handled = true;
                        try {
                            Class<?> loggerC = Class.forName("org.opennms.core.utils.ThreadCategory");

                            Class<?>[] methodParmList = { Class.class };
                            Method loggerM = loggerC.getMethod("getInstance", methodParmList);

                            Object[] parmList = { this.getClass() };
                            Object loggerI = loggerM.invoke(null, parmList);

                            methodParmList = new Class[] { Object.class, Throwable.class };
                            Method infoM = loggerC.getMethod("info", methodParmList);

                            parmList = new Object[] { "An unknown error occured decoding the packet", e };
                            infoM.invoke(loggerI, parmList);
                        } catch (Throwable t) {
                            handled = false;
                        }

                        if (!handled) {
                            System.out.println(new Date() + " - Exception: " + e.getMessage());
                        }
                        m_handler.processException(e);
                    }
                }

                // recycle the packet buffer if possible
                //
                if (pkt != null) {
                    synchronized (usedBuffers) {
                        // only keep 20 * 16k, or 520k worth
                        // of buffers around
                        //
                        if (usedBuffers.size() < 20)
                            usedBuffers.addLast(pkt.getData());
                    }
                }
            }
        } // end run()

    }

    /**
     * Recovers a SnmpPduPacket or SnmpPduTrap from the passed datagram and
     * calls the appropriate method in the handler.
     * 
     * If an error occurs recovering the packet then an exception is generated.
     * The pdu can be one of SnmpPduRequest or SnmpPduBulk. The internal session
     * AsnEncoder defined in the SnmpParameters is used to recover the pdu.
     * 
     * @param pkt
     *            The datagram packet to be decoded
     * 
     * @exception SnmpPduEncodingException
     *                Thrown if a pdu or session level error occurs
     * @exception AsnDecodingException
     *                Thrown if the AsnEncoder encounters an error
     * 
     * @see SnmpPduTrap
     * @see SnmpPduPacket
     * @see SnmpPduRequest
     * @see SnmpPduBulk
     * @see SnmpParameters
     * @see org.opennms.protocols.snmp.asn1.AsnEncoder
     * 
     */
    void handlePkt(DatagramPacket pkt) throws SnmpPduEncodingException, AsnDecodingException {
        //
        // first decode the header
        //
        byte[] buf = pkt.getData();
        int offset = 0;

        //
        // Decode the ASN.1 header from the front
        // of the SNMP message.
        //
        Object[] rVals = m_encoder.parseHeader(buf, offset);

        //
        // get the return vals
        //
        offset = ((Integer) rVals[0]).intValue();
        byte asnType = ((Byte) rVals[1]).byteValue();
        int asnLength = ((Integer) rVals[2]).intValue();

        //
        // check the ASN.1 Type
        //
        if (asnType != (ASN1.SEQUENCE | ASN1.CONSTRUCTOR))
            throw new AsnDecodingException("Invalid SNMP ASN.1 type");

        //
        // Check the length of the datagram packet
        //
        if (asnLength > pkt.getLength() - offset) {
            throw new SnmpPduEncodingException("Insufficent data in packet");
        }

        //
        // get the SNMP version.
        //
        SnmpInt32 int32 = new SnmpInt32();
        offset = int32.decodeASN(buf, offset, m_encoder);

        //
        // check the version
        //
        if (int32.getValue() != SnmpSMI.SNMPV1 && int32.getValue() != SnmpSMI.SNMPV2) {
            throw new SnmpPduEncodingException("Invalid protocol version");
        }

        //
        // need to get the community
        // Postpone the community check until the pdu
        // has been recovered to determine which community
        // string needs to be verified against.
        //
        SnmpOctetString community = new SnmpOctetString();
        offset = community.decodeASN(buf, offset, m_encoder);

        //
        // get the pdu header, but DO NOT modify the offset
        // in effect we are peeking into the remainder of the
        // packet
        //
        rVals = m_encoder.parseHeader(buf, offset);

        //
        // The command should be sign extended to a
        // negative number. Thus add 256 to wrap it
        //
        int cmd = ((Byte) rVals[1]).intValue() + 256;

        //
        // Now process the Protocol Data Unit
        //
        switch (cmd) {
        case SnmpPduPacket.SET:
        case SnmpPduPacket.GET:
        case SnmpPduPacket.GETNEXT:
        case SnmpPduPacket.RESPONSE:
        case SnmpPduPacket.INFORM:
        case SnmpPduPacket.V2TRAP:
        case SnmpPduPacket.REPORT: {
            SnmpPduPacket pdu = new SnmpPduRequest();
            offset = pdu.decodeASN(buf, offset, m_encoder);
            m_handler.processSnmpMessage(pkt.getAddress(), // From Who?
                                         pkt.getPort(), // What Port?
                                         int32, // What Version
                                         community, // Community
                                         cmd, // Snmp Command (Wrapped!)
                                         pdu); // The Protocol Data Unit
        }
            break;

        case SnmpPduPacket.GETBULK: {
            SnmpPduPacket pdu = new SnmpPduBulk();
            offset = pdu.decodeASN(buf, offset, m_encoder);
            m_handler.processSnmpMessage(pkt.getAddress(), // From Who?
                                         pkt.getPort(), // Port
                                         int32, // Version
                                         community, // Community
                                         cmd, // Command (positive wrapped)
                                         pdu); // Protocol Data Unit
        }
            break;

        case SnmpPduTrap.TRAP: {
            SnmpPduTrap trap = new SnmpPduTrap();
            offset = trap.decodeASN(buf, offset, m_encoder);
            m_handler.processSnmpTrap(pkt.getAddress(), pkt.getPort(), community, trap);
        }
            break;

        default:
            throw new SnmpPduEncodingException("No matching PDU type found");
        }
    }

    /**
     * Transmits the passed buffer to the respective peer agent. If a failure
     * occurs then an IOException is thrown.
     * 
     * @param peer
     *            The SNMP peer destination
     * @param buf
     *            The buffer to transmit.
     * @param length
     *            The valid length of the buffer
     * 
     * @exception java.lang.IOException
     *                For more details see java.net.DatagramSocket.
     * 
     * @see java.net.DatagramSocket
     * 
     */
    void send(SnmpPeer peer, byte[] buf, int length) throws java.io.IOException {
        //
        // create a new datagram packet
        //
        DatagramPacket pkt = new DatagramPacket(buf, length, peer.getPeer(), peer.getPort());

        m_comm.send(pkt);

    }

    /**
     * Transmits the passed buffer to the respective peer agent. If a failure
     * occurs then an IOException is thrown.
     * 
     * @param peer
     *            The SNMP peer destination
     * @param buf
     *            The buffer to transmit.
     * 
     * @exception java.lang.IOException
     *                For more details see java.net.DatagramSocket.
     * 
     * @see java.net.DatagramSocket
     * 
     */
    void send(SnmpPeer peer, byte[] buf) throws java.io.IOException {
        send(peer, buf, buf.length);
    }

    /**
     * Sets the default SnmpPacketHandler.
     * 
     * @param hdl
     *            The new handler
     * 
     */
    void setPacketHandler(SnmpPacketHandler hdl) {
        if (hdl == null)
            throw new IllegalArgumentException("The packet handler must not be null");

        m_handler = hdl;
    }

    /**
     * Gets the default SnmpPacketHandler for the session.
     * 
     * @return the SnmpPacketHandler
     */
    SnmpPacketHandler getPacketHandler() {
        return m_handler;
    }

    /**
     * Sets the default encoder.
     * 
     * @param encoder
     *            The new encoder
     * 
     */
    void setAsnEncoder(AsnEncoder encoder) {
        if (encoder == null)
            throw new IllegalArgumentException("The ASN.1 codec must not be null");

        m_encoder = encoder;
    }

    /**
     * Gets the AsnEncoder for the session.
     * 
     * @return the AsnEncoder
     */
    AsnEncoder getAsnEncoder() {
        return m_encoder;
    }

    /**
     * Returns true if this portal has had it's <CODE>close</CODE> method
     * called.
     * 
     */
    boolean isClosed() {
        return m_isClosing;
    }

    /**
     * Used to close the session. Once called the session should be considered
     * invalid and unusable.
     * 
     */
    void close() {
        m_isClosing = true;
        m_comm.close();

        try {
            //
            // make sure that the caller thread
            // is not the one we are trying to
            // join!
            //
            if (m_recvThread.equals(Thread.currentThread()) == false) {
                m_recvThread.join();
            }
        } catch (InterruptedException err) {
            Thread.currentThread().interrupt(); // reset the flag
        }
    }
}
