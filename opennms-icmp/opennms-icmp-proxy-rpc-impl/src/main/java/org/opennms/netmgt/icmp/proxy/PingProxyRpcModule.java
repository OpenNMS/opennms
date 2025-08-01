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
package org.opennms.netmgt.icmp.proxy;

import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.netmgt.icmp.PingerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PingProxyRpcModule extends AbstractXmlRpcModule<PingRequestDTO, PingResponseDTO>  {

    public static final String RPC_MODULE_ID = "PING";

    @Autowired
    private PingerFactory pingerFactory;

    public PingProxyRpcModule() {
        super(PingRequestDTO.class, PingResponseDTO.class);
    }

    @Override
    public CompletableFuture<PingResponseDTO> execute(PingRequestDTO request) {
        final PingResultTracker tracker = new PingResultTracker();
        try {
            pingerFactory.getInstance().ping(
                    request.getInetAddress(),
                    request.getTimeout(),
                    request.getRetries(),
                    request.getPacketSize(),
                    1,
                    tracker);
        } catch (Exception e) {
            tracker.completeExceptionally(e);
        }
        return tracker;
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    public void setPingerFactory(PingerFactory pingerFactory) {
        this.pingerFactory = pingerFactory;
    }

    @Override
    public PingResponseDTO createResponseWithException(Throwable ex) {
        return new PingResponseDTO(ex);
    }
}
