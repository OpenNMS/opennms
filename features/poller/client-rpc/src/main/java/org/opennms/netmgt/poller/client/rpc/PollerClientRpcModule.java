/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
