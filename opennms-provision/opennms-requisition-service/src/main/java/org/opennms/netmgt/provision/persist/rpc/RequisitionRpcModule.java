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
