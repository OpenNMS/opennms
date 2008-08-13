/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Aug 11: Converted to Trilead SSH client
 * 2007 Oct 03: Initial version
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.protocols.InsufficientParametersException;

import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class Ssh {
    // 30 second timeout
    public static final int DEFAULT_TIMEOUT = 30000;
    
    // SSH port is 22
    public static final int DEFAULT_PORT = 22;
    
    private SshClient m_client;
    private Throwable m_exception;
    private InetAddress m_address;
    private int m_port = DEFAULT_PORT;
    private int m_timeout = DEFAULT_TIMEOUT;
    private File m_keydir;
    private String m_username;
    private String m_password;
    private String m_serverVersion = "";

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
     * Set the timeout in milliseconds. 
     * @param milliseconds the timeout
     */
    public void setTimeout(int milliseconds) {
        m_timeout = milliseconds;
    }

    /**
     * Get the timeout in milliseconds.
     * @return the timeout
     */
    public int getTimeout() {
        return m_timeout;
    }
    
    /**
     * Set the directory to search for SSH keys.
     * @param directory the directory
     */
    public void setKeyDirectory(File directory) {
        m_keydir = directory;
    }
    
    /**
     * Get the directory to search for SSH keys.
     * @return the directory
     */
    public File getKeyDirectory() {
        return m_keydir;
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
     * Get the SSH server version banner.
     * @return the version string
     */
    public String getServerVersion() {
        return m_serverVersion;
    }

    /**
     * Attempt to connect, based on the parameters which have been set in
     * the object.
     * 
     * @return true if it is able to connect
     * @throws InsufficientParametersException
     */
    protected boolean connect() throws InsufficientParametersException {
        if (getAddress() == null) {
            throw new InsufficientParametersException("you must specify an address");
        }

        try {
            m_client = new SshClient();
            m_client.setSocketTimeout(getTimeout());
            SshConnectionProperties props = new SshConnectionProperties();
            props.setHost(getAddress().getHostAddress());
            props.setPort(getPort());
            m_client.connect(props, new IgnoreHostKeyVerification());
            if (getUsername() != null) {
                PasswordAuthenticationClient pac = new PasswordAuthenticationClient();
                pac.setUsername(getUsername());
                pac.setPassword(getPassword());
                int result = m_client.authenticate(pac);
                switch (result) {
                case AuthenticationProtocolState.READY:
                    m_client.disconnect();
                    return true;
                    case AuthenticationProtocolState.COMPLETE:
                        m_client.disconnect();
                        return true;
                    case AuthenticationProtocolState.CANCELLED:
                        log().error("the user cancelled authentication (this error should not occur)");
                        break;
                    case AuthenticationProtocolState.FAILED:
                        log().error("the authentication failed");
                        break;
                    case AuthenticationProtocolState.PARTIAL:
                        log().error("the authentication was rejected");
                        break;
                    default:
                        log().error("unknown AuthenticationProtocolState: " + result);
                        break;
                }
            } else {
                if (m_client.isConnected()) {
                    m_serverVersion = m_client.getServerId();
                    m_client.disconnect();
                    return true;
                } else {
                    log().error("client did not connect");
                }
            }
        } catch (IOException e) {
            log().debug("connection failed", e);
        } finally {
            if (m_client != null && m_client.isConnected()) {
                log().debug("client is still connected, disconnecting");
                m_client.disconnect();
            }
        }
        return false;
    }

    protected Throwable getError() {
        return m_exception;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
