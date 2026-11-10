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
package org.opennms.netmgt.poller.client.rpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PollerClientRpcModule extends AbstractXmlRpcModule<PollerRequestDTO, PollerResponseDTO> {

    public static final String RPC_MODULE_ID = "Poller";

    @Autowired
    private ServiceMonitorRegistry serviceMonitorRegistry;

    @Autowired
    @Qualifier("pollerExecutor")
    private Executor executor;

    public PollerClientRpcModule() {
        super(PollerRequestDTO.class, PollerResponseDTO.class);
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    @Override
    public CompletableFuture<PollerResponseDTO> execute(PollerRequestDTO request) {
        final String className = request.getClassName();
        final ServiceMonitor monitor = serviceMonitorRegistry.getMonitorByClassName(className);
        if (monitor == null) {
            return CompletableFuture.completedFuture(new PollerResponseDTO(PollStatus.unknown("No monitor found with class name '" + className + "'.")));
        }

        return CompletableFuture.supplyAsync(new Supplier<PollerResponseDTO>() {
            @Override
            public PollerResponseDTO get() {
                PollStatus pollStatus;
                try {
                    final Map<String, Object> parameters = request.getMonitorParameters();
                    pollStatus = monitor.poll(request, parameters);
                } catch (RuntimeException e) {
                    pollStatus = PollStatus.unknown(e.getMessage());
                }
                return new PollerResponseDTO(pollStatus);
            }
        }, executor);
    }

    public void setServiceMonitorRegistry(ServiceMonitorRegistry serviceMonitorRegistry) {
        this.serviceMonitorRegistry = serviceMonitorRegistry;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public PollerResponseDTO createResponseWithException(Throwable ex) {
        return new PollerResponseDTO(ex);
    }

}
