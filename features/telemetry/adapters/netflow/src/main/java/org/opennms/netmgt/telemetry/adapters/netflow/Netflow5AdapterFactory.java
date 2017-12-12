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

package org.opennms.netmgt.telemetry.adapters.netflow;

import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.CacheSettings;
import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.adapters.api.AdapterFactory;
import org.opennms.netmgt.telemetry.config.api.Protocol;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;

public class Netflow5AdapterFactory implements AdapterFactory {
    private MetricRegistry metricRegistry;
    private InterfaceToNodeCache interfaceToNodeCache;
    private NodeDao nodeDao;
    private TransactionOperations transactionOperations;
    private FlowRepository flowRepository;
    private CacheSettings nodeInfoCacheSettings;

    @Override
    public Class<? extends Adapter> getAdapterClass() {
        return Netflow5Adapter.class;
    }

    @Override
    public Adapter createAdapter(Protocol protocol, Map<String, String> properties) {
        Objects.requireNonNull(interfaceToNodeCache);
        Objects.requireNonNull(metricRegistry);
        Objects.requireNonNull(nodeDao);
        Objects.requireNonNull(transactionOperations);
        Objects.requireNonNull(flowRepository);
        Objects.requireNonNull(nodeInfoCacheSettings);

        final Netflow5Adapter adapter = new Netflow5Adapter();
        adapter.setInterfaceToNodeCache(interfaceToNodeCache);
        adapter.setMetricRegistry(metricRegistry);
        adapter.setNodeDao(nodeDao);
        adapter.setFlowRepository(flowRepository);
        adapter.setNodeInfoCacheSettings(nodeInfoCacheSettings);
        adapter.setTransactionOperations(transactionOperations);
        adapter.setProtocol(protocol);

        adapter.init();

        return adapter;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void setInterfaceToNodeCache(InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    public void setFlowRepository(FlowRepository flowRepository) {
        this.flowRepository = flowRepository;
    }

    public void setNodeInfoCacheSettings(CacheSettings nodeInfoCacheSettings) {
        this.nodeInfoCacheSettings = nodeInfoCacheSettings;
    }
}
