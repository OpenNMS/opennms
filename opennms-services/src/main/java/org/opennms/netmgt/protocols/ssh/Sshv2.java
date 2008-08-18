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
public class Sshv2 extends Ssh {
    private SshClient m_client;

    public Sshv2() { }
    
    public Sshv2(InetAddress address) {
        setAddress(address);
    }
    
    public Sshv2(InetAddress address, int port) {
        setAddress(address);
        setPort(port);
    }
    
    public Sshv2(InetAddress address, int port, int timeout) {
        setAddress(address);
        setPort(port);
        setTimeout(timeout);
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
                        logError("Authentication: the user cancelled authentication (this error should not occur)");
                        break;
                    case AuthenticationProtocolState.FAILED:
                        logError("Authentication: the authentication failed");
                        break;
                    case AuthenticationProtocolState.PARTIAL:
                        logError("Authentication: the authentication was rejected");
                        break;
                    default:
                        logError("Authentication: unknown protocol state: " + result);
                        break;
                }
            } else {
                if (m_client.isConnected()) {
                    m_serverBanner = m_client.getServerId();
                    m_client.disconnect();
                    return true;
                } else {
                    log().warn("client did not connect (reason unknown)");
                }
            }
        } catch (IOException e) {
            logError("connection failed", e);
        } finally {
            if (m_client != null && m_client.isConnected()) {
                log().debug("client is still connected, disconnecting");
                m_client.disconnect();
            }
        }
        return false;
    }

    private Throwable logError(String message) {
        return logError(message, null);
    }

    private Throwable logError(String message, Throwable error) {
        if (error == null) {
            error = new SshException(message);
        }
        setError(error);
        log().error(error);
        return error;
    }
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}