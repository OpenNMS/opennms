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

package org.opennms.netmgt.provision.detector.loop.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.loop.response.LoopResponse;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.support.Client;

/**
 * <p>LoopClient class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class LoopClient implements Client<LineOrientedRequest, LoopResponse> {
    
    private String m_address;
    private boolean m_isSupported = false;
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        
    }

    /** {@inheritDoc} */
    @Override
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        m_address = InetAddressUtils.str(address);
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.detector.loop.response.LoopResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public LoopResponse receiveBanner() throws IOException, Exception {
        return receiveResponse();
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest} object.
     * @return a {@link org.opennms.netmgt.provision.detector.loop.response.LoopResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public LoopResponse sendRequest(LineOrientedRequest request) throws IOException, Exception {
        return null;
    }
    
    private LoopResponse receiveResponse() {
        LoopResponse loopResponse = new LoopResponse();
        loopResponse.receive(m_address, isSupported());
        return loopResponse;
    }

    /**
     * <p>setSupported</p>
     *
     * @param isSupported a boolean.
     */
    public void setSupported(boolean isSupported) {
        m_isSupported = isSupported;
    }

    /**
     * <p>isSupported</p>
     *
     * @return a boolean.
     */
    public boolean isSupported() {
        return m_isSupported;
    }

}
