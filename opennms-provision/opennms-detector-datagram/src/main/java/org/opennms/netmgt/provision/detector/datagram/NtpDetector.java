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
package org.opennms.netmgt.provision.detector.datagram;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.netmgt.provision.detector.datagram.client.NtpClient;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.RequestBuilder;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
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

    private ResponseValidator<DatagramPacket> validateResponse(final InetAddress nserver) {
        return new ResponseValidator<DatagramPacket>(){

            public boolean validate(DatagramPacket response) throws Exception {
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

    private RequestBuilder<NtpMessage> createNtpMessage() {
        return new RequestBuilder<NtpMessage>(){

            public NtpMessage getRequest() throws Exception {
                return new NtpMessage();
            }
            
        };
    }
    
    private InetAddress getAddress(){
        try {
            return InetAddress.getByName(getIpToValidate());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * <p>setIpToValidate</p>
     *
     * @param ipToValidate a {@link java.lang.String} object.
     */
    public void setIpToValidate(String ipToValidate) {
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
