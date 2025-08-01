/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.detector.datagram;

import java.io.IOException;
import java.net.DatagramPacket;

import org.opennms.netmgt.provision.detector.datagram.client.DatagramClient;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.dns.DNSAddressRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DnsDetector class.</p>
 *
 * @author brozow
 * @version $Id: $
 */

public class DnsDetector extends BasicDetector<DatagramPacket, DatagramPacket> {
    
    private static final Logger LOG = LoggerFactory.getLogger(DnsDetector.class);
    private static final String DEFAULT_SERVICE_NAME = "DNS";

    private static final int DEFAULT_PORT = 53;
    private static final String DEFAULT_LOOKUP = "localhost";
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
    public DnsDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }
    
    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        final DNSAddressRequest req = addrRequest(getLookup());
        send(encode(req), verifyResponse(req));
    }
    
    /**
     * @param request
     * @return
     */
    private static ResponseValidator<DatagramPacket> verifyResponse(final DNSAddressRequest request) {
        
        return new ResponseValidator<DatagramPacket>() {

            @Override
            public boolean validate(final DatagramPacket response) {
                
                try {
                    request.verifyResponse(response.getData(), response.getLength());
                } catch (final IOException e) {
                    LOG.info("failed to connect", e);
                    return false;
                } 
                
                return true;
            }
            
        };
    }
    
    private static DNSAddressRequest addrRequest(final String host) {
        return new DNSAddressRequest(host);
    }
    
    private static DatagramPacket encode(final DNSAddressRequest dnsPacket) {
        final byte[] data = buildRequest(dnsPacket);
        return new DatagramPacket(data, data.length);
    }

    /**
     * @param request
     * @return
     * @throws IOException
     */
    private static byte[] buildRequest(final DNSAddressRequest request) {
        try {
            return request.buildRequest();
        } catch (final IOException e) {
            // this shouldn't really happen
            throw new IllegalStateException("Unable to build dnsRequest!!! This shouldn't happen!!");
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
    public void setLookup(final String lookup) {
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
}
