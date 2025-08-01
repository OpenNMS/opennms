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

import java.net.DatagramPacket;

import org.opennms.netmgt.provision.detector.datagram.client.NtpClient;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.RequestBuilder;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.ntp.NtpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NtpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NtpDetector extends BasicDetector<NtpMessage, DatagramPacket> {
    
    private static final Logger LOG = LoggerFactory.getLogger(NtpDetector.class);
    private final NtpClient m_client;
    
    /**
     * <p>Constructor for NtpDetector.</p>
     */
    public NtpDetector() {
        super("NTP", 123);
        m_client = new NtpClient();
    }

    /** {@inheritDoc} */
    @Override
    protected Client<NtpMessage, DatagramPacket> getClient() {
        return m_client;
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        send(createNtpMessage(), validateResponse());
    }

    private ResponseValidator<DatagramPacket> validateResponse() {
        return new ResponseValidator<DatagramPacket>(){

            @Override
            public boolean validate(final DatagramPacket response) {
                if (response.getAddress().equals(m_client.getAddress())) {
                    // Parse the incoming data
                    NtpMessage m = new NtpMessage(response.getData());
                    LOG.info("NTP message received {}", m.toString());
                    // All timestamps returned on the package are required in order to process the NTP package on the client side.
                    return m.originateTimestamp > 0 && m.transmitTimestamp > 0 && m.referenceTimestamp > 0 && m.receiveTimestamp > 0;
                }else{
                    return false;
                }
            }
            
        };
    }

    private RequestBuilder<NtpMessage> createNtpMessage() {
        return new RequestBuilder<NtpMessage>(){

            @Override
            public NtpMessage getRequest() {
                return new NtpMessage();
            }
            
        };
    }

    public void setIpToValidate(String address) {
        // This method only exists for compatibility purposes, this won't be used. The address to be used will be the one defined on the client.
    }

    public String getIpToValidate() {
        // This method only exists for compatibility purposes, this won't be used. The address to be used will be the one defined on the client.
        return null;
    }

}
