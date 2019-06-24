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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.ipfix;

import java.util.Objects;

import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.adapter.AdapterFactory;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;

import com.codahale.metrics.MetricRegistry;

public class IpfixAdapterFactory implements AdapterFactory {
    private MetricRegistry metricRegistry;
    private FlowRepository flowRepository;

    @Override
    public Class<? extends Adapter> getBeanClass() {
        return IpfixAdapter.class;
    }

    @Override
    public Adapter createBean(final AdapterDefinition adapterConfig) {
        Objects.requireNonNull(metricRegistry);
        Objects.requireNonNull(flowRepository);

        return new IpfixAdapter(metricRegistry, flowRepository);
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void setFlowRepository(FlowRepository flowRepository) {
        this.flowRepository = flowRepository;
    }
}
