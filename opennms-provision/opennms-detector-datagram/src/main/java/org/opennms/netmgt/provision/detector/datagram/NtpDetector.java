/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.datagram;

import java.net.DatagramPacket;
import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.detector.datagram.client.NtpClient;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.RequestBuilder;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.opennms.netmgt.provision.support.ntp.NtpMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>NtpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class NtpDetector extends BasicDetector<NtpMessage, DatagramPacket> {
    
    private String m_ipToValidate;
    
    /**
     * <p>Constructor for NtpDetector.</p>
     */
    public NtpDetector() {
        super("NTP", 123);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<NtpMessage, DatagramPacket> getClient() {
        return new NtpClient();
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        send(createNtpMessage(), validateResponse(getAddress()));
    }

    private static ResponseValidator<DatagramPacket> validateResponse(final InetAddress nserver) {
        return new ResponseValidator<DatagramPacket>(){

            public boolean validate(final DatagramPacket response) {
                if (response.getAddress().equals(nserver)) {
                    // parse the incoming data
                    new NtpMessage(response.getData());
                    return true;
                }else{
                    return false;
                }
            }
            
        };
    }

    private static RequestBuilder<NtpMessage> createNtpMessage() {
        return new RequestBuilder<NtpMessage>(){

            public NtpMessage getRequest() {
                return new NtpMessage();
            }
            
        };
    }
    
    private InetAddress getAddress() {
    	final InetAddress addr = InetAddressUtils.addr(getIpToValidate());
    	if (addr == null) {
    		LogUtils.debugf(this, "Failed to get InetAddress from %s", getIpToValidate());
    	}
    	return addr;
    }
    
    /**
     * <p>setIpToValidate</p>
     *
     * @param ipToValidate a {@link java.lang.String} object.
     */
    public void setIpToValidate(final String ipToValidate) {
        m_ipToValidate = ipToValidate;
    }

    /**
     * <p>getIpToValidate</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpToValidate() {
        return m_ipToValidate;
    }

}
