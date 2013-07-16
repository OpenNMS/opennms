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

package org.opennms.protocols.dhcp.detector.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.netmgt.dhcpd.Dhcpd;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.protocols.dhcp.detector.request.DhcpRequest;
import org.opennms.protocols.dhcp.detector.response.DhcpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>DhcpClient class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DhcpClient implements Client<DhcpRequest, DhcpResponse> {
	
	private static final Logger LOG = LoggerFactory.getLogger(DhcpClient.class);

    
    private int m_retries;
    private int m_timeout;
    private InetAddress m_address;
    private long m_responseTime;
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        
    }

    /** {@inheritDoc} */
    @Override
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        m_address = address;
        m_timeout = timeout;
        
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.protocols.dhcp.detector.response.DhcpResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public DhcpResponse receiveBanner() throws IOException, Exception {
        m_responseTime = Dhcpd.isServer(m_address, m_timeout, getRetries());
        LOG.debug("got a response from the server: {}", m_responseTime);
        DhcpResponse response = new DhcpResponse(m_responseTime);
        return response;
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.protocols.dhcp.detector.request.DhcpRequest} object.
     * @return a {@link org.opennms.protocols.dhcp.detector.response.DhcpResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public DhcpResponse sendRequest(DhcpRequest request) throws IOException, Exception {
        return null;
    }
    
    /**
     * <p>setRetries</p>
     *
     * @param retries a int.
     */
    public void setRetries(int retries) {
        m_retries = retries;
    }

    /**
     * <p>getRetries</p>
     *
     * @return a int.
     */
    public int getRetries() {
        return m_retries;
    }

}
