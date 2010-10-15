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
 * Modifications:
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */

package org.opennms.netmgt.provision.detector.datagram;

import java.io.IOException;
import java.net.DatagramPacket;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.detector.datagram.client.DatagramClient;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.dns.DNSAddressRequest;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>DnsDetector class.</p>
 *
 * @author brozow
 * @version $Id: $
 */

@Component
@Scope("prototype")
public class DnsDetector extends BasicDetector<DatagramPacket, DatagramPacket> {
    
    private static final String DEFAULT_SERVICE_NAME = "DNS";

    private final static int DEFAULT_PORT = 53;
    private final static String DEFAULT_LOOKUP = "localhost";
    private String m_lookup = DEFAULT_LOOKUP;

    /**
     * Default constructor
     */
    public DnsDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }
    
    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public DnsDetector(String serviceName, int port) {
        super(serviceName, port);
    }
    
    /**
     * <p>onInit</p>
     */
    public void onInit() {
        DNSAddressRequest req;
        send(encode(req = addrRequest(getLookup())), verifyResponse(req));
    }
    
    /**
     * @param request
     * @return
     */
    private ResponseValidator<DatagramPacket> verifyResponse(final DNSAddressRequest request) {
        
        return new ResponseValidator<DatagramPacket>() {

            public boolean validate(DatagramPacket response) {
                
                try {
                    request.verifyResponse(response.getData(), response.getLength());
                } catch (IOException e) {
                    info("DNSDetector failed to connect");
                    return false;
                } 
                
                return true;
            }
            
        };
    }
    
    private DNSAddressRequest addrRequest(String host) {
        return new DNSAddressRequest(host);
    }
    
    private DatagramPacket encode(DNSAddressRequest dnsPacket) {
        byte[] data = buildRequest(dnsPacket);
        return new DatagramPacket(data, data.length);
    }

    /**
     * @param request
     * @return
     * @throws IOException
     */
    private byte[] buildRequest(DNSAddressRequest request) {
        try {
            return request.buildRequest();
        } catch (IOException e) {
            // this can't really happen
            throw new IllegalStateException("Unable to build dnsRequest!!! This can't happen!!");
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.BasicDetector#getClient()
     */
    /** {@inheritDoc} */
    @Override
    protected Client<DatagramPacket, DatagramPacket> getClient() {
        return new DatagramClient();
    }

    /**
     * <p>setLookup</p>
     *
     * @param lookup the lookup to set
     */
    public void setLookup(String lookup) {
        m_lookup = lookup;
    }

    /**
     * <p>getLookup</p>
     *
     * @return the lookup
     */
    public String getLookup() {
        return m_lookup;
    }
    
    private void info(String format, Object... args) {
        ThreadCategory log = ThreadCategory.getInstance(getClass());
        if (log.isInfoEnabled()) {
            log.info(String.format(format, args));
        }
    }

}
