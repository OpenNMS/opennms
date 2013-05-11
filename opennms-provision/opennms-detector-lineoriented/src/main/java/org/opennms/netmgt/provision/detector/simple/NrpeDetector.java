/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.simple;

import org.opennms.netmgt.provision.detector.simple.client.NrpeClient;
import org.opennms.netmgt.provision.detector.simple.request.NrpeRequest;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
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
    private String m_command = NrpePacket.HELLO_COMMAND;

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
        send(request(m_command), resultCodeEquals(0));
    }
    
    private static ResponseValidator<NrpePacket> resultCodeEquals(final int desiredResultCode){
        return new ResponseValidator<NrpePacket>() {

            @Override
            public boolean validate(final NrpePacket response) {
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

    /**
     * <p>setCommand</p>
     *
     * @param command a String.
     */
    public void setCommand(final String command) {
        this.m_command = command;
    }

    /**
     * <p>getCommand</p>
     *
     * @return a String.
     */
    public String getCommand() {
        return m_command;
    }

}
