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

package org.opennms.netmgt.dhcpd;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.dhcpd.DhcpdConfigFactory;
import org.opennms.netmgt.utils.IpValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.bucknell.net.JDHCP.DHCPMessage;

/**
 * <P>
 * Establishes a TCP socket connection with the DHCP daemon and formats and
 * sends request messages.
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version CVS 1.1.1.1
 */
final class Poller {
	
	private static final Logger LOG = LoggerFactory.getLogger(Poller.class);

    /**
     * The hardware address (ex: 00:06:0D:BE:9C:B2)
     */
    private static final byte[] DEFAULT_MAC_ADDRESS = { (byte) 0x00,
            (byte) 0x06, (byte) 0x0d, (byte) 0xbe, (byte) 0x9c, (byte) 0xb2,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    private static byte[] s_hwAddress = null;
    private static byte[] s_myIpAddress = null;
    private static byte[] s_requestIpAddress = null;
    private static boolean reqTargetIp = true;
    private static boolean targetOffset = true;
    private static boolean relayMode = false;
    private static boolean paramsChecked = false;
    private static Boolean extendedMode = false;

    /**
     * Broadcast flag...when set in the 'flags' portion of the DHCP query
     * packet, it forces the DHCP server to broadcast the DHCP response. This
     * is useful when we are not setting the relay address in the outgoing
     * DHCP query. Otherwise, we would not receive the response.
     */
    static final short BROADCAST_FLAG = (short) 0x8000;

    /**
     * Default retries
     */
    static final int DEFAULT_RETRIES = 2;

    /**
     * Default timeout
     */
    static final long DEFAULT_TIMEOUT = 3000L;

    /**
     * The message type option for the DHCP request.
     */
    private static final int MESSAGE_TYPE = 53;

    /**
     * The requested ip option for the DHCP request.
     */
    private static final int REQUESTED_IP = 50;

    /**
     * Holds the value for the next identifier sent to the DHCP server.
     */
    private static int m_nextXid = (new java.util.Random(System.currentTimeMillis())).nextInt();

    /**
     * TCP Socket connection with DHCP Daemon
     */
    private Socket m_connection;

    /**
     * Output Object stream
     */
    private ObjectOutputStream m_outs;

    /**
     * Objects from the server.
     */
    private ObjectInputStream m_ins;

    /**
     * Returns a disconnection request message that can be sent to the server.
     * 
     * @return A disconnection message.
     */
    private static Message getDisconnectRequest() throws UnknownHostException {
        return new Message(InetAddressUtils.addr("0.0.0.0"), new DHCPMessage());
    }

    /**
     * Returns a DHCP DISCOVER, INFORM, or REQUEST message that can be sent to
     * the DHCP server. DHCP server should respond with a DHCP OFFER, ACK, or
     * NAK message in response..
     * 
     * @param (InetAddress) addr The address to poll
     * @param (byte) mType The type of DHCP message to send (DISCOVER, INFORM,
     *        or REQUEST)
     * @return The message to send to the DHCP server.
     */
    private static Message getPollingRequest(InetAddress addr, byte mType) {
        int xid = 0;
        synchronized (Poller.class) {
            xid = ++m_nextXid;
        }
        DHCPMessage messageOut = new DHCPMessage();
        byte[] rawIp = addr.getAddress();
        // if targetOffset = true, we don't want to REQUEST the DHCP server's
        // own IP, so change it by 1, trying to avoid the subnet address
        // and the broadcast address.
        if (targetOffset) {
            if (rawIp[3] % 2 == 0 && rawIp[3] != 0) {
                --rawIp[3];
            } else {
                ++rawIp[3];
            }
        }
        // fill DHCPMessage object
        //
        messageOut.setOp((byte) 1);
        messageOut.setHtype((byte) 1);
        messageOut.setHlen((byte) 6);
        messageOut.setXid(xid);
        messageOut.setSecs((short) 0);
        messageOut.setChaddr(s_hwAddress); // set hardware address
        if (relayMode) {
            messageOut.setHops((byte) 1);
            messageOut.setGiaddr(s_myIpAddress); // set relay address for replies
        } else {
            messageOut.setHops((byte) 0);
            messageOut.setFlags(BROADCAST_FLAG);
        }

        messageOut.setOption(MESSAGE_TYPE, new byte[] { mType });
        if (mType == DHCPMessage.REQUEST) {
            if (reqTargetIp) {
                messageOut.setOption(REQUESTED_IP, rawIp);
                messageOut.setCiaddr(rawIp);
            } else {
                messageOut.setOption(REQUESTED_IP, s_requestIpAddress);
                messageOut.setCiaddr(s_requestIpAddress);
            }
        }
        if (mType == DHCPMessage.INFORM) {
            messageOut.setOption(REQUESTED_IP, s_myIpAddress);
            messageOut.setCiaddr(s_myIpAddress);
        }

        return new Message(addr, messageOut);
    }

    /**
     * Ensures that during garbage collection the resources used by this
     * object are released!
     *
     * @throws java.lang.Throwable if any.
     */
    @Override
    protected void finalize() throws Throwable {
        close();
    }

    /**
     * Constructor. Establishes a TCP socket connection with the DHCP client
     * daemon on port 5818.
     * 
     * @throws IOException
     *             if unable to establish the connection with the DHCP client
     *             daemon.
     */
    private Poller(long timeout) throws IOException {
        DhcpdConfigFactory dcf = DhcpdConfigFactory.getInstance();
        try {
                LOG.debug("Poller.ctor: opening socket connection with DHCP client daemon on port {}", dcf.getPort());
            m_connection = new Socket(InetAddressUtils.addr("127.0.0.1"), dcf.getPort());

                LOG.debug("Poller.ctor: setting socket timeout to {}", timeout);
            m_connection.setSoTimeout((int) timeout);

            // Establish input/output object streams
            m_ins = new ObjectInputStream(m_connection.getInputStream());
            m_outs = new ObjectOutputStream(m_connection.getOutputStream());
            m_outs.reset();
            m_outs.flush();
        } catch (IOException ex) {
            LOG.error("IO Exception during socket connection establishment with DHCP client daemon.", ex);
            if (m_connection != null) {
                try {
                    m_ins.close();
                    m_outs.close();
                    m_connection.close();
                } catch (Throwable t) {
                }
            }
            throw ex;
        } catch (Throwable t) {
            LOG.error("Unexpected exception during socket connection establishment with DHCP client daemon.", t);
            if (m_connection != null) {
                try {
                    m_ins.close();
                    m_outs.close();
                    m_connection.close();
                } catch (Throwable tx) {
                }
            }
            throw new UndeclaredThrowableException(t);
        }
    }

    /**
     * Closes the client's socket connection to the DHCP daemon.
     *
     * @throws IOException
     *             if the socket close() method fails.
     */
    public void close() {
        try {
                LOG.debug("Closing connection");
            m_ins.close();
            m_outs.close();
            m_connection.close();
        } catch (Throwable ex) {
        }
    }

    /**
     * <p>
     * This method actually tests the remote host to determine if it is
     * running a functional DHCP server.
     * </p>
     * <p>
     * Formats a DHCP query and encodes it in a client request message which
     * is sent to the DHCP daemon over the established TCP socket connection.
     * If a matching DHCP response packet is not received from the DHCP daemon
     * within the specified timeout the client request message will be re-sent
     * up to the specified number of retries.
     * </p>
     * <p>
     * If a response is received from the DHCP daemon it is validated to
     * ensure that:
     * </p>
     * <ul>
     * <li>The DHCP response packet was sent from the remote host to which the
     * original request packet was directed.</li>
     * <li>The XID of the DHCP response packet matches the XID of the original
     * DHCP request packet.</li>
     * </ul>
     * <p>
     * If the response validates 'true' is returned. Otherwise the request is
     * resent until max retry count is exceeded.
     * </p>
     * <p>
     * Before returning, a client disconnect message (remote host field set to
     * zero) is sent to the DHCP daemon.
     * </p>
     * 
     * @return response time in milliseconds if the specified host responded
     *         with a valid DHCP offer datagram within the context of the
     *         specified timeout and retry values or negative one (-1)
     *         otherwise.
     */
    static long isServer(InetAddress host, long timeout, int retries) throws IOException {

        boolean isDhcpServer = false;
        // List of DHCP queries to try. The default when extended
        // mode = false must be listed first. (DISCOVER)
        byte[] typeList = { (byte) DHCPMessage.DISCOVER, (byte) DHCPMessage.INFORM, (byte) DHCPMessage.REQUEST };
        String[] typeName = { "DISCOVER", "INFORM", "REQUEST" };
        DhcpdConfigFactory dcf = DhcpdConfigFactory.getInstance();
        if (!paramsChecked) {
            String s_extendedMode = dcf.getExtendedMode();
            if (s_extendedMode == null) {
                extendedMode = false;
            } else {
                extendedMode = Boolean.parseBoolean(s_extendedMode);
            }
                LOG.debug("isServer: DHCP extended mode is {}", extendedMode);
            
            String hwAddressStr = dcf.getMacAddress();
                LOG.debug("isServer: DHCP query hardware/MAC address is {}", hwAddressStr);
            setHwAddress(hwAddressStr);
            
            String myIpStr = dcf.getMyIpAddress();
                LOG.debug("isServer: DHCP relay agent address is {}", myIpStr);
            if (myIpStr == null || myIpStr.equals("") || myIpStr.equalsIgnoreCase("broadcast")) {
                // do nothing
            } else if (IpValidator.isIpValid(myIpStr)) {
                s_myIpAddress = setIpAddress(myIpStr);
                relayMode = true;
            }
            
            if (extendedMode == true) {
                String requestStr = dcf.getRequestIpAddress();
                    LOG.debug("isServer: REQUEST query target is {}", requestStr);
                if (requestStr == null || requestStr.equals("") || requestStr.equalsIgnoreCase("targetSubnet")) {
                    // do nothing
                } else if (requestStr.equalsIgnoreCase("targetHost")) {
                    targetOffset = false;
                } else if (IpValidator.isIpValid(requestStr)) {
                    s_requestIpAddress = setIpAddress(requestStr);
                    reqTargetIp = false;
                    targetOffset = false;
                }
                    LOG.debug("REQUEST query options are: reqTargetIp = {}, targetOffset = {}", reqTargetIp, targetOffset);
            }
            paramsChecked = true;
        }

        int j = 1;
        if (extendedMode == true) {
            j = typeList.length;
        }

        if (timeout < 500) {
            timeout = 500;
        }

        Poller p = new Poller(timeout);
        long responseTime = -1;
        try {
            pollit: for (int i = 0; i < j; i++) {

                Message ping = getPollingRequest(host, (byte) typeList[i]);

                int rt = retries;
                while (rt >= 0 && !isDhcpServer) {
                        LOG.debug("isServer: sending DHCP {} query to host {} with Xid: {}", typeName[i], InetAddressUtils.str(host), ping.getMessage().getXid());
                    
                    long start = System.currentTimeMillis();
                    p.m_outs.writeObject(ping);
                    long end;

                    do {
                        Message resp = null;
                        try {
                            resp = (Message) p.m_ins.readObject();
                        } catch (InterruptedIOException ex) {
                            resp = null;
                        }

                        if (resp != null) {
                            responseTime = System.currentTimeMillis() - start;

                            // DEBUG only
                                LOG.debug("isServer: got a DHCP response from host {} with Xid: {}", InetAddressUtils.str(resp.getAddress()), resp.getMessage().getXid());

                            if (host.equals(resp.getAddress()) && ping.getMessage().getXid() == resp.getMessage().getXid()) {
                                // Inspect response message to see if it is a valid DHCP response
                                byte[] type = resp.getMessage().getOption(MESSAGE_TYPE);
                                    if (type[0] == DHCPMessage.OFFER) {
                                        LOG.debug("isServer: got a DHCP OFFER response, validating...");
                                    } else if (type[0] == DHCPMessage.ACK) {
                                        LOG.debug("isServer: got a DHCP ACK response, validating...");
                                    } else if (type[0] == DHCPMessage.NAK) {
                                        LOG.debug("isServer: got a DHCP NAK response, validating...");
                                    }

                                // accept offer or ACK or NAK
                                if (type[0] == DHCPMessage.OFFER || (extendedMode == true && (type[0] == DHCPMessage.ACK || type[0] == DHCPMessage.NAK))) {
                                        LOG.debug("isServer: got a valid DHCP response. responseTime= {}ms", responseTime);
                                    
                                    isDhcpServer = true;
                                    break pollit;
                                }
                            }
                        }

                        end = System.currentTimeMillis();

                    } while ((end - start) < timeout);

                        if (!isDhcpServer) {
                            LOG.debug("Timed out waiting for DHCP response, remaining retries: {}", rt);
                        }

                    --rt;
                }
            }

                LOG.debug("Sending disconnect request");
            p.m_outs.writeObject(getDisconnectRequest());
                LOG.debug("wait half a sec before closing connection");
            Thread.sleep(500);
            p.close();
        } catch (IOException ex) {
            LOG.error("IO Exception caught.", ex);
            p.close();
            throw ex;
        } catch (Throwable t) {
            LOG.error("Unexpected Exception caught.", t);
            p.close();
            throw new UndeclaredThrowableException(t);
        }

        // Return response time if the remote box IS a DHCP
        // server or -1 if the remote box is NOT a DHCP server.
        if (isDhcpServer) {
            return responseTime;
        } else {
            return -1;
        }
    }

    // Converts the provided hardware address string (format=00:00:00:00:00:00)
    // to an array of bytes which can be passed in a DHCP DISCOVER packet.
    private static void setHwAddress(String hwAddressStr) {
        // initialize the address
        s_hwAddress = DEFAULT_MAC_ADDRESS;

        StringTokenizer token = new StringTokenizer(hwAddressStr, ":");
        if (token.countTokens() != 6) {
                LOG.debug("Invalid format for hwAddress {}", hwAddressStr);
        }
        int temp;
        int i = 0;
        while (i < 6) {
            try {
                temp = Integer.parseInt(token.nextToken(), 16);
                s_hwAddress[i] = (byte) temp;
                i++;
            } catch (NumberFormatException ex) {
                    LOG.debug("Invalid format for hwAddress, {}", ex);
            }
        }
    }

    // Converts the provided IP address string
    // to an array of bytes which can be passed in a DHCP packet.

    private static byte[] setIpAddress(String ipAddressStr) {
        // initialize the address
        byte[] ipAddress = new byte[4];
        StringTokenizer token = new StringTokenizer(ipAddressStr, ".");
        int temp;
        int i = 0;
        while (i < 4) {
            temp = Integer.parseInt(token.nextToken(), 10);
            ipAddress[i] = (byte) temp;
            i++;
        }
        return ipAddress;
    }
}
