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

package org.opennms.netmgt.provision.persist.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.opennms.core.rpc.xml.AbstractXmlRpcModule;
import org.opennms.netmgt.provision.persist.RequisitionProvider;
import org.opennms.netmgt.provision.persist.RequisitionProviderRegistry;
import org.opennms.netmgt.provision.persist.RequisitionRequest;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class RequisitionRpcModule extends AbstractXmlRpcModule<RequisitionRequestDTO, RequisitionResponseDTO> {

    public static final String RPC_MODULE_ID = "Requisition";

    @Autowired
    private RequisitionProviderRegistry registry;

    @Autowired
    @Qualifier("requisitionRequestExecutor")
    private Executor executor;

    public RequisitionRpcModule() {
        super(RequisitionRequestDTO.class, RequisitionResponseDTO.class);
    }

    @Override
    public String getId() {
        return RPC_MODULE_ID;
    }

    @Override
    public RequisitionResponseDTO createResponseWithException(Throwable ex) {
        return new RequisitionResponseDTO(ex);
    }

    @Override
    public CompletableFuture<RequisitionResponseDTO> execute(RequisitionRequestDTO request) {
        return CompletableFuture.supplyAsync(new Supplier<RequisitionResponseDTO>() {
            @Override
            public RequisitionResponseDTO get() {
                // Lookup the provider in the thread, since the lookup may block
                final RequisitionProvider provider = registry.getProviderByType(request.getType());
                if (provider == null) {
                    throw new IllegalArgumentException("No provider found for type: " + request.getType());
                }
                // Retrieve the request from the DTO (possibly unmarshaling it)
                final RequisitionRequest providerRequest = request.getProviderRequest(provider);
                // Retrieve the requisition
                final Requisition requisition = provider.getRequisition(providerRequest);
                return new RequisitionResponseDTO(requisition);
            }
        }, executor);
    }

    public void setRegistry(RequisitionProviderRegistry registry) {
        this.registry = registry;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
