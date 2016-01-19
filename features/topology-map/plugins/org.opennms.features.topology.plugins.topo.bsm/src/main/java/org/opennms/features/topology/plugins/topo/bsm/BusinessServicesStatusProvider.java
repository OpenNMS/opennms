/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.bsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultStatus;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.vaadin.core.TransactionAwareBeanProxyFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class BusinessServicesStatusProvider implements StatusProvider {

    private final TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory;
    private BusinessServiceManager businessServiceManager;

    public BusinessServicesStatusProvider(TransactionAwareBeanProxyFactory transactionAwareBeanProxyFactory) {
        this.transactionAwareBeanProxyFactory = Objects.requireNonNull(transactionAwareBeanProxyFactory);
    }

    @Override
    public Map<VertexRef, Status> getStatusForVertices(VertexProvider vertexProvider, Collection<VertexRef> vertices, Criteria[] criteria) {
        // filter out vertices from other providers
        final Collection<VertexRef> filteredVertices = Collections2.filter(vertices, new Predicate<VertexRef>() {
            @Override
            public boolean apply(VertexRef input) {
                return input instanceof AbstractBusinessServiceVertex && contributesTo(input.getNamespace());
            }
        });

        // cast to AbstractBusinessServiceVertex
        final Collection<AbstractBusinessServiceVertex> businessServiceVertices = Collections2.transform(filteredVertices, new Function<VertexRef, AbstractBusinessServiceVertex>() {
            @Override
            public AbstractBusinessServiceVertex apply(VertexRef input) {
                return (AbstractBusinessServiceVertex) input;
            }
        });

        final Map<VertexRef, Status> statusMap = new HashMap<>();
        for (AbstractBusinessServiceVertex eachVertex : businessServiceVertices) {
            final OnmsSeverity operationalStatus = getOperationalStatus(eachVertex);
            statusMap.put(eachVertex, new DefaultStatus(operationalStatus.getLabel(), 0));
        }
        return statusMap;
    }

    private OnmsSeverity getOperationalStatus(AbstractBusinessServiceVertex vertex) {
        if (vertex instanceof BusinessServiceVertex) {
            BusinessServiceVertex bsVertex = (BusinessServiceVertex) vertex;
            return businessServiceManager.getBusinessServiceById(bsVertex.getServiceId()).getOperationalStatus();
        }
        if (vertex instanceof IpServiceVertex) {
            IpServiceVertex ipServiceVertex = (IpServiceVertex) vertex;
            return businessServiceManager.getOperationalStatusForIPService(ipServiceVertex.getIpServiceId());
        }
        throw new IllegalStateException("Unsupported BusinessServiceVertex type: " + vertex.getClass());
    }

    @Override
    public String getNamespace() {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace().equals(namespace);
    }

    public void setBusinessServiceManager(BusinessServiceManager serviceManager) {
        Objects.requireNonNull(serviceManager);
        this.businessServiceManager = transactionAwareBeanProxyFactory.createProxy(serviceManager);
    }
}
