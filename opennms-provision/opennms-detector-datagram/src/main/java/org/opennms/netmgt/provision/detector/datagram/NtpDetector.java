/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
