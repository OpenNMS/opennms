/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Aug 11: Fixes for bug 2574
 * 2007 Oct 03: Initial version
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.protocols.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.protocols.InsufficientParametersException;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class Ssh extends org.opennms.netmgt.protocols.AbstractPoll {
    
    // SSH port is 22
    public static final int DEFAULT_PORT = 22;

    // Default to 1.99 (v1 + v2 support)
    public static final String DEFAULT_CLIENT_BANNER = "SSH-1.99-OpenNMS_1.5";

    protected int m_port = DEFAULT_PORT;
    protected String m_username;
    protected String m_password;
    protected String m_banner = DEFAULT_CLIENT_BANNER;
    protected String m_serverBanner = "";
    protected InetAddress m_address;
    protected Throwable m_error;
    
    private Socket m_socket = null;
    private BufferedReader m_reader = null;
    private OutputStream m_writer = null;

    public Ssh() { }
    
    public Ssh(InetAddress address) {
        setAddress(address);
    }
    
    public Ssh(InetAddress address, int port) {
        setAddress(address);
        setPort(port);
    }
    
    public Ssh(InetAddress address, int port, int timeout) {
        setAddress(address);
        setPort(port);
        setTimeout(timeout);
    }

    /**
     * Set the address to connect to.
     * 
     * @param address the address
     */
    public void setAddress(InetAddress address) {
        m_address = address;
    }
 
    /**
     * Get the address to connect to.
     * @return the address
     */
    public InetAddress getAddress() {
        return m_address;
    }

    /**
     * Set the port to connect to.
     * @param port the port
     */
    public void setPort(int port) {
        m_port = port;
    }
    
    /**
     * Get the port to connect to.
     * @return the port
     */
    public int getPort() {
        if (m_port == 0) {
            return 22;
        }
        return m_port;
    }
    
    /**
     * Set the username to connect as.
     * @param username the username
     */
    public void setUsername(String username) {
        m_username = username;
    }
    
    /**
     * Get the username to connect as.
     * @return the username
     */
    public String getUsername() {
        return m_username;
    }
    
    /**
     * Set the password to connect with.
     * @param password the password
     */
    public void setPassword(String password) {
        m_password = password;
    }

    /**
     * Get the password to connect with.
     * @return the password
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * Set the banner string to use when connecting
     * @param banner the banner
     */
    public void setClientBanner(String banner) {
        m_banner = banner;
    }

    /**
     * Get the banner string used when connecting
     * @return the banner
     */
    public String getClientBanner() {
        return m_banner;
    }
    
    /**
     * Get the SSH server version banner.
     * @return the version string
     */
    public String getServerBanner() {
        return m_serverBanner;
    }

    protected void setError(Throwable t) {
        m_error = t;
    }
    
    protected Throwable getError() {
        return m_error;
    }

    /**
     * Attempt to connect, based on the parameters which have been set in
     * the object.
     * 
     * @return true if it is able to connect
     * @throws InsufficientParametersException
     */
    protected boolean tryConnect() throws InsufficientParametersException {
        if (getAddress() == null) {
            throw new InsufficientParametersException("you must specify an address");
        }

        try {
            m_socket = new Socket();
            m_socket.setTcpNoDelay(true);
            m_socket.connect(new InetSocketAddress(getAddress(), getPort()), getTimeout());
            m_socket.setSoTimeout(getTimeout());
            
            m_reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
            m_writer = m_socket.getOutputStream();

            // read the banner
            m_serverBanner = m_reader.readLine();

            // write our own
            m_writer.write((getClientBanner() + "\r\n").getBytes());

            // then, disconnect
            disconnect();

            return true;
        } catch (NumberFormatException e) {
            log().debug("unable to parse server version", e);
            setError(e);
            disconnect();
        } catch (ConnectException e) {
            log().debug("connection failed: " + e.getMessage());
            setError(e);
            disconnect();
        } catch (Exception e) {
            log().debug("connection failed", e);
            setError(e);
            disconnect();
        }
        return false;
    }

    protected void disconnect() {
        if (m_writer != null) {
            try {
                m_writer.close();
            } catch (IOException e) {
                log().warn("error disconnecting output stream", e);
            }
        }
        if (m_reader != null) {
            try {
                m_reader.close();
            } catch (IOException e) {
                log().warn("error disconnecting input stream", e);
            }
        }
        if (m_socket != null) {
            try {
                m_socket.close();
            } catch (IOException e) {
                log().warn("error disconnecting socket", e);
            }
        }
    }

    public PollStatus poll(TimeoutTracker tracker) throws InsufficientParametersException {
        tracker.startAttempt();
        boolean isAvailable = tryConnect();
        double responseTime = tracker.elapsedTimeInMillis();

        PollStatus ps = PollStatus.unavailable();
        
        String errorMessage = "";
        if (getError() != null) {
            errorMessage = getError().getMessage();
            ps.setReason(errorMessage);
        }

        if (isAvailable) {
            ps = PollStatus.available(responseTime);
        } else if (errorMessage.matches("^.*Authentication:.*$")) {
            ps = PollStatus.unavailable("authentication failed");
        } else if (errorMessage.matches("^.*java.net.NoRouteToHostException.*$")) {
            ps = PollStatus.unavailable("no route to host");
        } else if (errorMessage.matches("^.*(timeout: socket is not established|java.io.InterruptedIOException|java.net.SocketTimeoutException).*$")) {
            ps = PollStatus.unavailable("connection timed out");
        } else if (errorMessage.matches("^.*(connection is closed by foreign host|java.net.ConnectException).*$")) {
            ps = PollStatus.unavailable("connection exception");
        } else if (errorMessage.matches("^.*NumberFormatException.*$")) {
            ps = PollStatus.unavailable("an error occurred parsing the server version number");
        } else if (errorMessage.matches("^.*java.io.IOException.*$")) {
            ps = PollStatus.unavailable("I/O exception");
        }
        
        return ps;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
