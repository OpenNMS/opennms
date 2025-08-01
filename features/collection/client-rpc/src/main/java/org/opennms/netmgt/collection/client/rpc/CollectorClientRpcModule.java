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
package org.opennms.netmgt.collection.client.rpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.opennms.core.logging.Logging;
import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceCollectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Used to perform collections via {@link ServiceCollector}s.
 *
 * @author jwhite
 */
public class CollectorClientRpcModule extends AbstractXmlRpcModule<CollectorRequestDTO, CollectorResponseDTO>{

    public static final String RPC_MODULE_ID = "Collect";

    @Autowired
    private ServiceCollectorRegistry serviceCollectorRegistry;

    @Autowired
    @Qualifier("collectorExecutor")
    private Executor executor;

    public CollectorClientRpcModule() {
        super(CollectorRequestDTO.class, CollectorResponseDTO.class);        
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    @Override
    public CompletableFuture<CollectorResponseDTO> execute(CollectorRequestDTO request) {
        final String className = request.getClassName();
        final ServiceCollector collector = serviceCollectorRegistry.getCollectorFutureByClassName(className).getNow(null);
        if (collector == null) {
            throw new IllegalArgumentException("No collector found with class name '" + className + "'.");
        }

        return CompletableFuture.supplyAsync(new Supplier<CollectorResponseDTO>() {
            @Override
            public CollectorResponseDTO get() {
                Logging.putPrefix("collectd");
                final CollectionAgent agent = request.getAgent();
                final Map<String, Object> parameters = request.getParameters(collector);
                return new CollectorResponseDTO(collector.collect(agent, parameters));
            }
        }, executor);
    }

    public void setServiceCollectorRegistry(ServiceCollectorRegistry serviceCollectorRegistry) {
        this.serviceCollectorRegistry = serviceCollectorRegistry;
    }

    public ServiceCollectorRegistry getServiceCollectorRegistry() {
        return serviceCollectorRegistry;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public CollectorResponseDTO createResponseWithException(Throwable ex) {
        return new CollectorResponseDTO(ex);
    }

}
