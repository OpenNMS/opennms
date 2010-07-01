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
package org.opennms.netmgt.provision.detector.smb.client;

import java.io.IOException;
import java.net.InetAddress;

import jcifs.netbios.NbtAddress;

import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.smb.response.NbtAddressResponse;
import org.opennms.netmgt.provision.support.Client;

/**
 * <p>SmbClient class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class SmbClient implements Client<LineOrientedRequest, NbtAddressResponse>{
    
    private NbtAddress m_nbtAddress;
    private String m_address;
    
    /**
     * <p>close</p>
     */
    public void close() {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
       m_address = address.getHostAddress();
       m_nbtAddress = NbtAddress.getByName(m_address);
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.detector.smb.response.NbtAddressResponse} object.
     * @throws java.io.IOException if any.
     */
    public NbtAddressResponse receiveBanner() throws IOException {
        return receiveResponse();
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     * @return a {@link org.opennms.netmgt.provision.detector.smb.response.NbtAddressResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    public NbtAddressResponse sendRequest(LineOrientedRequest request) throws IOException, Exception {
        return receiveResponse();
    }
    
    private NbtAddressResponse receiveResponse() {
        NbtAddressResponse nbtAddrResponse = new NbtAddressResponse();
        nbtAddrResponse.receive(m_address, m_nbtAddress);
        return nbtAddrResponse;
    }

}
