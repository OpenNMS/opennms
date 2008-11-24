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
package org.opennms.netmgt.provision.detector;

import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.nrpe.NrpePacket;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;

/**
 * @author Donald Desloge
 *
 */
public class NrpeDetector extends BasicDetector<NrpeRequest, NrpePacket> {
    
    private final static int DEFAULT_PORT = 5666;
    
    /**
     * Default number of retries for TCP requests
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for TCP requests
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds
    
    /**
     * Default whether to use SSL
     */
    private final static boolean DEFAULT_USE_SSL = true;
    
    private boolean m_useSsl = DEFAULT_USE_SSL;
    private int m_padding = 2;
    
    protected NrpeDetector() {
        super(DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRY);
        setServiceName("NRPE");
    }

    @Override
    protected void onInit() {
        send(request(NrpePacket.HELLO_COMMAND), resultCodeEquals(0));
    }
    
    private ResponseValidator<NrpePacket> resultCodeEquals(final int desiredResultCode){
        return new ResponseValidator<NrpePacket>() {

            public boolean validate(NrpePacket response) throws Exception {
                if(response.getResultCode() == desiredResultCode) {
                    return true;
                }
                return false;
            }
            
        };
    }
    
    /**
     * @return
     */
    private NrpeRequest request(String command) {
        NrpePacket packet = new NrpePacket(NrpePacket.QUERY_PACKET, (short) 0, command);
        byte[] b = packet.buildPacket(getPadding());
        return new NrpeRequest(b);
    }

    @Override
    protected Client<NrpeRequest, NrpePacket> getClient() {
        NrpeClient client = new NrpeClient();
        client.setPadding(getPadding());
        client.setUseSsl(isUseSsl());
        return client;
    }


    public void setUseSsl(boolean useSsl) {
        m_useSsl = useSsl;
    }

    public boolean isUseSsl() {
        return m_useSsl;
    }

    public void setPadding(int padding) {
        m_padding = padding;
    }

    public int getPadding() {
        return m_padding;
    }

}
