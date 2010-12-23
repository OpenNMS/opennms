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
package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.client.NrpeClient;
import org.opennms.netmgt.provision.detector.simple.request.NrpeRequest;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.nrpe.NrpePacket;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * <p>NrpeDetector class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
@Component
@Scope("prototype")
public class NrpeDetector extends BasicDetector<NrpeRequest, NrpePacket> {
    
    private static final String DEFAULT_SERVICE_NAME = "NRPE";

    private final static int DEFAULT_PORT = 5666;
    
    /**
     * Default whether to use SSL
     */
    private final static boolean DEFAULT_USE_SSL = true;
    
    private boolean m_useSsl = DEFAULT_USE_SSL;
    private int m_padding = 2;

    /**
     * Default constructor
     */
    public NrpeDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /**
     * Constructor for creating a non-default service based on this protocol
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    public NrpeDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        send(request(NrpePacket.HELLO_COMMAND), resultCodeEquals(0));
    }
    
    private ResponseValidator<NrpePacket> resultCodeEquals(final int desiredResultCode){
        return new ResponseValidator<NrpePacket>() {

            public boolean validate(final NrpePacket response) throws Exception {
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
    private NrpeRequest request(final String command) {
        final NrpePacket packet = new NrpePacket(NrpePacket.QUERY_PACKET, (short) 0, command);
        final byte[] b = packet.buildPacket(getPadding());
        return new NrpeRequest(b);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<NrpeRequest, NrpePacket> getClient() {
        final NrpeClient client = new NrpeClient();
        client.setPadding(getPadding());
        client.setUseSsl(isUseSsl());
        return client;
    }


    /**
     * <p>setUseSsl</p>
     *
     * @param useSsl a boolean.
     */
    public void setUseSsl(final boolean useSsl) {
        m_useSsl = useSsl;
    }

    /**
     * <p>isUseSsl</p>
     *
     * @return a boolean.
     */
    public boolean isUseSsl() {
        return m_useSsl;
    }

    /**
     * <p>setPadding</p>
     *
     * @param padding a int.
     */
    public void setPadding(final int padding) {
        m_padding = padding;
    }

    /**
     * <p>getPadding</p>
     *
     * @return a int.
     */
    public int getPadding() {
        return m_padding;
    }

}
