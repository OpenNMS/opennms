/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.simple.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPSocketFactory;

/**
 * @author thedesloge
 *
 */
public class LdapDetectorClient extends LineOrientedClient {
    
    /**
     * A class to add a timeout to the socket that the LDAP code uses to access
     * an LDAP server
     */
    private static class TimeoutLDAPSocket implements LDAPSocketFactory {

        private final int m_timeout;
        private Socket m_socket;

        public TimeoutLDAPSocket(int timeout) {
            m_timeout = timeout;
        }

        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            m_socket = new Socket(host, port);
            m_socket.setSoTimeout(m_timeout);
            return m_socket;
        }
    }
    
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        super.connect(address, port, timeout);
        LDAPConnection lc = new LDAPConnection(new TimeoutLDAPSocket(timeout));
        lc.connect(address.getHostAddress(), port);

    }

}
