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
package org.opennms.netmgt.provision.detector.smb.client;

import java.io.IOException;
import java.net.InetAddress;

import jcifs.CIFSContext;
import jcifs.NameServiceClient;
import jcifs.context.BaseContext;
import jcifs.context.SingletonContext;
import jcifs.netbios.NameServiceClientImpl;
import jcifs.netbios.NbtAddress;

import org.opennms.core.utils.InetAddressUtils;
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
    private NameServiceClient m_nsc;
    private String m_address;
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    @Override
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
       CIFSContext base = SingletonContext.getInstance();
       m_nsc = new NameServiceClientImpl(base);
       m_address = InetAddressUtils.str(address);
       m_nbtAddress = (NbtAddress) m_nsc.getNbtByName(m_address);
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.detector.smb.response.NbtAddressResponse} object.
     * @throws java.io.IOException if any.
     */
    @Override
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
    @Override
    public NbtAddressResponse sendRequest(LineOrientedRequest request) throws IOException, Exception {
        return receiveResponse();
    }
    
    private NbtAddressResponse receiveResponse() {
        NbtAddressResponse nbtAddrResponse = new NbtAddressResponse();
        nbtAddrResponse.receive(m_address, m_nbtAddress);
        return nbtAddrResponse;
    }

}
