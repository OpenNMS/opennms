/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.provision.detector.dhcp.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dhcpd.Dhcpd;
import org.opennms.netmgt.provision.detector.dhcp.request.DhcpRequest;
import org.opennms.netmgt.provision.detector.dhcp.response.DhcpResponse;
import org.opennms.netmgt.provision.support.Client;


public class DhcpClient implements Client<DhcpRequest, DhcpResponse> {
    
    private int m_retries;
    private int m_timeout;
    private InetAddress m_address;
    private long m_responseTime;
    
    public void close() {
        
    }

    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        m_address = address;
        m_timeout = timeout;
        
    }

    public DhcpResponse receiveBanner() throws IOException, Exception {
        m_responseTime = Dhcpd.isServer(m_address, m_timeout, getRetries());
        ThreadCategory.getInstance(DhcpClient.class).debug("got a response from the server: " + m_responseTime);
        DhcpResponse response = new DhcpResponse(m_responseTime);
        return response;
    }

    public DhcpResponse sendRequest(DhcpRequest request) throws IOException, Exception {
        return null;
    }
    
    public void setRetries(int retries) {
        m_retries = retries;
    }

    public int getRetries() {
        return m_retries;
    }

}
