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
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.dhcpd.DhcpdConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.utils.IpValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * The DHCP client daemon serves as a multiplexor for DHCP requests and
 * responses. The Bootp/DHCP protocol specifies that a DHCP server listens for
 * requests on local UDP/67 and will either send/broadcast responses to UDP/68
 * or UDP/67 or will unicast responses back to the client's UDP port on from
 * which the request originated.
 * </p>
 *
 * <p>
 * The DHCP daemon accepts client connections on TCP/5818. Once a client is
 * connected it can begin forwarding requests. A list of all currently connected
 * clients is maintained. Requests have the following format:
 * </p>
 * <ul>
 * <li>byte 1 - byte 4 : 32-bit remote host IP address</li>
 * <li>byte 5 - byte 8 : 32-bit buffer length</li>
 * <li>byte 9 - byte n : buffer containing the formatted DHCP discover request.
 * </li>
 * </ul>
 *
 * <p>
 * The client indicates that it is finished by sending a request with the remote
 * host IP address set to zero (0).
 * </p>
 *
 * <p>
 * Incoming requests are sent to UDP/67 on specified remote host. If the remote
 * host is runnning a DHCP server it will send/broadcast an appropriate response
 * to UDP/68 or UDP/67 (or will unicast the response).
 * </p>
 *
 * <p>
 * The DHCP daemon includes a listener thread which binds to UDP/68 or UDP/67
 * and simply listens for any incoming DHCP responses. In extended mode,
 * threads are started on both ports. When a datagram is received by the
 * listener thread(s) it loops through the list of currently connected clients
 * and forwards the DHCP response packet to each client. It is the responsibility
 * of the client to validate that the datagram is in response to a DHCP request
 * packet that it generated.
 * </p>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public final class Dhcpd extends AbstractServiceDaemon implements Runnable, Observer {

    private static final Logger LOG = LoggerFactory.getLogger(Dhcpd.class);


    /**
     * The singular instance of the DHCP server.
     */
    private final static Dhcpd m_singleton = new Dhcpd();

    /**
     * List of clients currently connected to the DHCP daemon
     */
    private static List<Client> m_clients;

    /**
     * Socket over which the daemon actively listens for new client connection
     * requests.
     */
    private ServerSocket m_server;

    /**
     * DHCP response port 68 listener.
     */
    private Receiver m_listener;

    /**
     * DHCP response port 67 listener.
     */
    private Receiver2 m_listener2;

    /**
     * The working thread
     */
    private Thread m_worker;

    /**
     * Constructs a new DHCP server instance. All of the internal fields are
     * initialized to <code>null</code>.
     * 
     */
    private Dhcpd() {
    	super("dhcpd");
        m_clients = null;
        m_server = null;
        m_listener = null;
        m_listener2 = null;
        m_worker = null;
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        boolean relayMode = false;
        LOG.debug("start: DHCP client daemon starting...");

        // Only allow start to be called once.
        if (m_worker != null && m_worker.isAlive()) {
            throw new IllegalStateException("The server is already running");
        }

        // Unless the worker has died, then stop and continue
        if (m_worker != null) {
            stop();
        }

        // the client list
        m_clients = Collections.synchronizedList(new LinkedList<Client>());

        // load the dhcpd configuration
        DhcpdConfigFactory dFactory = null;
        try {
            DhcpdConfigFactory.reload();
            dFactory = DhcpdConfigFactory.getInstance();
        } catch (Exception ex) {
            LOG.error("Failed to load dhcpd configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        // open the server
        //
        try {
            LOG.debug("start: listening on TCP port {} for incoming client requests.", dFactory.getPort());
            m_server = new ServerSocket(dFactory.getPort(), 0, InetAddressUtils.addr("127.0.0.1"));
        } catch (IOException ex) {
            if (ex instanceof java.net.BindException) {
                LOG.error("Failed to listen on DHCP port, perhaps something else is already listening?", ex);
                LOG.error("Failed to listen on DHCP port, perhaps something else is already listening?", ex);
            } else {
                LOG.error("Failed to initialize DHCP socket", ex);
            }
            throw new UndeclaredThrowableException(ex);
        }

        // see if we have a valid relay address
        String myIpStr = DhcpdConfigFactory.getInstance().getMyIpAddress();
        LOG.debug("Checking string \"{}\" to see if we have an IP address", myIpStr);
        if (myIpStr != null &&  !myIpStr.equals("") && !myIpStr.equalsIgnoreCase("broadcast")) {
            if(IpValidator.isIpValid(myIpStr)) {
                relayMode = true;
            }
        }
        LOG.debug("Setting relay mode {}", relayMode);
        
        // open the receiver socket(s)
        if(!relayMode || (dFactory.getExtendedMode() != null && dFactory.getExtendedMode().equalsIgnoreCase("true"))) {
            try {
                LOG.debug("start: starting receiver thread for port 68");
                m_listener = new Receiver(m_clients);
                m_listener.start();
            } catch (IOException ex) {
                try {
                    m_server.close();
                } catch (IOException ex1) {
                }
                throw new UndeclaredThrowableException(ex);
            }
        }

        if(relayMode) {
            try {
                LOG.debug("start: starting receiver thread for port 67");
                m_listener2 = new Receiver2(m_clients);
                m_listener2.start();
            } catch (IOException ex) {
                try {
                    m_server.close();
                } catch (IOException ex1) {
                }
                throw new UndeclaredThrowableException(ex);
            }
        }

        m_worker = new Thread(this, getName());
        m_worker.start();
	}

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
	if (m_worker == null) {
            return;
        }
        
        // stop the receiver
        if (m_listener != null) {
            m_listener.stop();
        }
        
        // stop the other receiver
        if (m_listener2 != null) {
        	m_listener2.stop();
        }

        // close the server socket
        try {
            m_server.close();
        } catch (IOException ex) {
        }

        // close all the clients
        Object[] list = null;
        synchronized (m_clients) {
            list = m_clients.toArray();
        }

        for (int x = 0; list != null && x < list.length; x++) {
            ((Client) list[x]).stop();
        }

        m_server = null;
        m_clients = null;
        m_worker = null;
        m_listener = null;
        m_listener2 = null;
    }

    /**
     * The main routine of the DHCP server. This method accepts incoming client
     * requests and starts new client handlers to process each request.
     */
    @Override
    public void run() {
        
        try {
            waitForStatus(RUNNING);
        } catch (InterruptedException e1) {
            // ignore
        }
        
        LOG.debug("run: DHCPD client daemon running...");
        
        /*
         * Begin accepting connections from clients
         * For each new client create new DHCP Client Handler
         * thread to handle the client's requests.
         */
        try {
            m_server.setSoTimeout(1000); // Wake up every second to check the
                                            // status

            for (;;) {
                synchronized (this) {
                    if (isPaused()) {
                        try {
                            waitForStatus(RUNNING);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    } else if (!isRunning()) {
                        break;
                    }
                }

                Socket sock;
                try {
                    sock = m_server.accept();
                } catch (InterruptedIOException iE) {
                    continue;
                }

                // Add the client's new socket connection to the client list
                LOG.debug("run: got connection request...creating client handler...");

                try {
                    Client clnt = new Client(sock);
                    m_clients.add(clnt);
                    clnt.addObserver(this);
                    clnt.start();
                } catch (IOException ioE) {
                    LOG.error("I/O exception occured creating client handler.", ioE);
                }
            }
        } catch (IOException ioE) {
            LOG.error("I/O exception occured processing incoming request", ioE);
        } catch (Throwable t) {
            LOG.error("An undeclared throwable was caught", t);
        } finally {
            LOG.debug("run: DHCPD client daemon run completed setting status to stopped");
        }

    }

    /**
     * {@inheritDoc}
     *
     * This method is called by the observable instances that the server has
     * registered to receive.
     */
    @Override
    public void update(Observable inst, Object ignored) {
        synchronized (this) {
            if (m_clients != null) {
                m_clients.remove(inst);
            }
        }
    }

    /**
     * Returns the singular instance of the DHCP server.
     *
     * @return a {@link org.opennms.netmgt.dhcpd.Dhcpd} object.
     */
    public static Dhcpd getInstance() {
        return m_singleton;
    }

    /**
     * Contacts the public server and checks to see if the the passed address is
     * a DHCP server.
     *
     * @param address
     *            The address to query.
     * @param timeout
     *            The time to wait between retries.
     * @param retries
     *            The maximum number of attempts.
     * @return response time in milliseconds if remote box is a DHCP server or
     *         -1 if it is NOT.
     * @throws java.io.IOException
     *             Thrown if an error occurs.
     */
    public static long isServer(InetAddress address, long timeout, int retries) throws IOException {
        return Poller.isServer(address, timeout, retries);
    }

    /**
     * Contacts the public server and checks to see if the the passed address is
     * a DHCP server.
     *
     * @param address
     *            The address to query.
     * @return response time in milliseconds if remote box is a DHCP server or
     *         -1 if it is NOT.
     * @throws java.io.IOException
     *             Thrown if an error occurs.
     */
    public static long isServer(InetAddress address) throws IOException {
        return Poller.isServer(address, Poller.DEFAULT_TIMEOUT, Poller.DEFAULT_RETRIES);
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
    	
    }
}
