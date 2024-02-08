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
