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

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.icmp.EchoPacket;
import org.opennms.netmgt.icmp.PingResponseCallback;

public class PingResultTracker extends CompletableFuture<PingResponseDTO> implements PingResponseCallback {

    @Override
    public void handleResponse(InetAddress address, EchoPacket response) {
        PingResponseDTO responseDTO = new PingResponseDTO();
        responseDTO.setRtt(response.elapsedTime(TimeUnit.MILLISECONDS));
        complete(responseDTO);
    }

    @Override
    public void handleTimeout(InetAddress address, EchoPacket request) {
        PingResponseDTO responseDTO = new PingResponseDTO();
        responseDTO.setRtt(Double.POSITIVE_INFINITY);
        if (!isDone()) {
            complete(responseDTO);
        }
    }

    @Override
    public void handleError(InetAddress address, EchoPacket request, Throwable t) {
        if (!isDone()) {
            completeExceptionally(t);
        }
    }
}
