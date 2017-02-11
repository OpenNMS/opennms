/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collection.client.rpc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.opennms.core.logging.Logging;
import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
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
        final ServiceCollector collector = serviceCollectorRegistry.getCollectorByClassName(className);
        if (collector == null) {
            throw new IllegalArgumentException("No collector found with class name '" + className + "'.");
        }

        return CompletableFuture.supplyAsync(new Supplier<CollectorResponseDTO>() {
            @Override
            public CollectorResponseDTO get() {
                Logging.putPrefix("collectd");
                final CollectionAgent agent = request.getAgent();
                final Map<String, Object> parameters = request.getParameters(collector);
                try {
                    return new CollectorResponseDTO(collector.collect(agent, parameters));
                } catch (CollectionException e) {
                    throw new RuntimeException(e);
                }
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
