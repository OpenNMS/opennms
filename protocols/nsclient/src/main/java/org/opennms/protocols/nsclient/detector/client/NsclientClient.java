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
                final StringBuilder message = new StringBuilder();
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
