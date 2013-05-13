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

import java.net.InetAddress;

/**
 * The peer object defines a SNMP peer agent that is communicated with. The
 * SnmpPeer object is used by the SnmpSession class to define the remote agent.
 * 
 * The information includes the peer's port and address. Also included is the
 * number of retries and timeouts that should be used when sending packets to
 * the agent.
 * 
 * @see SnmpSession
 * @see SnmpPeer
 * 
 * @author <a href="mailto:weave@oculan.com>Brian Weaver </a>
 * @version 1.1.1.1
 * 
 */
public class SnmpPeer extends Object implements Cloneable {
    /**
     * The internet address of the peer
     */
    private InetAddress m_peer; // the remote agent

    /**
     * The remote port of the agent. By default this is usually 161, but it can
     * change.
     */
    private int m_port; // the remote port

    /**
     * The local port of the agent. By default this is usually 0 when acting as
     * manager, 161 as agent
     */
    private int m_serverport = 0; // the local server port

    /**
     * The number of time to resend the datagram to the host.
     */
    private int m_retries; // # of retries

    /**
     * The length of time to wait on the remote agent to respond. The time is
     * measured in milliseconds (1/1000th of a second).
     */
    private int m_timeout; // in milliseconds

    /**
     * The default parameters for communicating with the agent. These include
     * the read/write community string and the SNMP protocol version.
     */
    private SnmpParameters m_params;

    /**
     * The default remote port. On most systems this is port 161, the default
     * trap receiver is on port 162.
     */
    public static final int defaultRemotePort = 161;

    /**
     * The library default for the number of retries.
     */
    public static final int defaultRetries = 3;

    /**
     * The library default for the number of milliseconds to wait for a reply
     * from the remote agent.
     */
    public static final int defaultTimeout = 8000; // .8 seconds

    /**
     * Class constructor. Constructs a SnmpPeer to the passed remote agent.
     * 
     * @param peer
     *            The remote internet address
     */
    public SnmpPeer(InetAddress peer) {
        m_peer = peer;
        m_port = defaultRemotePort;
        m_timeout = defaultTimeout;
        m_retries = defaultRetries;
        m_params = new SnmpParameters();
    }

    /**
     * Class constructor. Constructs a peer object with the specified internet
     * address and port.
     * 
     * @param peer
     *            The remote agent address
     * @param port
     *            The SNMP port on the remote
     * 
     */
    public SnmpPeer(InetAddress peer, int port) {
        this(peer);
        m_port = port;
    }

    /**
     * Class copy constructor. Constructs a SnmpPeer object that is identical to
     * the passed SnmpPeer object.
     * 
     * @param second
     *            The peer object to copy.
     * 
     */
    public SnmpPeer(SnmpPeer second) {
        m_peer = second.m_peer;
        m_port = second.m_port;
        m_timeout = second.m_timeout;
        m_retries = second.m_retries;
        m_params = (SnmpParameters) second.m_params.clone();
    }

    /**
     * Returns the peer agent's internet address to the caller
     * 
     * @return The peer's internet address
     * 
     */
    public InetAddress getPeer() {
        return m_peer;
    }

    /**
     * Used to set the peer's internet address for the remote agent.
     * 
     * @param addr
     *            The remote agents internet address
     * 
     */
    public void setPeer(InetAddress addr) {
        m_peer = addr;
    }

    /**
     * Used to set the peer's internet address and port for communications.
     * 
     * @param addr
     *            The remote agent's internet address
     * @param port
     *            The remote agent's port
     * 
     */
    public void setPeer(InetAddress addr, int port) {
        m_peer = addr;
        m_port = port;
    }

    /**
     * Returns the remote agent's port for communications
     * 
     * @return The remote agent's port
     */
    public int getPort() {
        return m_port;
    }

    /**
     * Used to set the remote communication port
     * 
     * @param port
     *            The remote communication port
     * 
     */
    public void setPort(int port) {
        m_port = port;
    }

    /**
     * Returns the local agent's port for communications
     * 
     * @return The local agent's port
     */
    public int getServerPort() {
        return m_serverport;
    }

    /**
     * Used to set the local communication port
     * 
     * @param port
     *            The local communication port
     * 
     */
    public void setServerPort(int port) {
        m_serverport = port;
    }

    /**
     * Returns the currently set number of retries defined by this peer
     * 
     * @return The currently configured number of retries.
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * Used to set the default number of retries for this peer agent.
     * 
     * @param retry
     *            The new number of retries for the peer
     * 
     */
    public void setRetries(int retry) {
        m_retries = retry;
    }

    /**
     * Retreives the currently configured timeout for the remote agent in
     * milliseconds (1/1000th second).
     * 
     * @return The timeout value in milliseconds.
     * 
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * Sets the millisecond timeout for the communications with the remote
     * agent.
     * 
     * @param timeout
     *            The timeout in milliseconds
     * 
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    /**
     * Retuns the current parameters for the peer agent.
     * 
     * @return The current SNMP parameters
     * 
     */
    public SnmpParameters getParameters() {
        return m_params;
    }

    /**
     * Used to set the current parameters for the SnmpPeer object.
     * 
     * @param params
     *            The SnmpParameters for the peer.
     * 
     */
    public void setParameters(SnmpParameters params) {
        m_params = params;
    }

    /**
     * Used to get a newly created copy of the current object.
     * 
     * @return A duplicate peer object.
     * 
     */
    @Override
    public Object clone() {
        return new SnmpPeer(this);
    }
}
