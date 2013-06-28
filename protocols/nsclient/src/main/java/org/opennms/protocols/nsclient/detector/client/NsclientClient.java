/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.nsclient.detector.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.protocols.nsclient.NsclientException;
import org.opennms.protocols.nsclient.NsclientManager;
import org.opennms.protocols.nsclient.NsclientPacket;
import org.opennms.protocols.nsclient.detector.request.NsclientRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NsclientClient class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class NsclientClient implements Client<NsclientRequest, NsclientPacket> {
	
	private static final Logger LOG = LoggerFactory.getLogger(NsclientClient.class);


    private String password;

    private NsclientManager client;

    @Override
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        client = new NsclientManager(InetAddressUtils.str(address), port, password);
        client.setTimeout(timeout);
        client.init();
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public NsclientPacket receiveBanner() throws IOException, Exception {
        return null;
    }

    @Override
    public NsclientPacket sendRequest(NsclientRequest request) throws IOException, Exception {
        boolean isAServer = false;
        NsclientPacket response = null;
        for (int attempts = 0; attempts <= request.getRetries() && !isAServer; attempts++) {
            try {
                response = client.processCheckCommand(request.getFormattedCommand(), request.getCheckParams());
                LOG.debug("sendRequest: {}: {}", request.getFormattedCommand(), response.getResponse());
                isAServer = true;
            } catch (NsclientException e) {
                StringBuffer message = new StringBuffer();
                message.append("sendRequest: Check failed... NsclientManager returned exception: ");
                message.append(e.getMessage());
                message.append(" : ");
                message.append((e.getCause() == null ? "": e.getCause().getMessage()));
                LOG.info(message.toString());
                isAServer = false;
            }
        }
        return response;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
