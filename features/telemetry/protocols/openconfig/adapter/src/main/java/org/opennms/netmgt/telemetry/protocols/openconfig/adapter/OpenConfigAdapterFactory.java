/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractCollectionAdapterFactory;
import org.osgi.framework.BundleContext;

public class OpenConfigAdapterFactory extends AbstractCollectionAdapterFactory {


    public OpenConfigAdapterFactory(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public Class<? extends Adapter> getBeanClass() {
        return OpenConfigAdapter.class;
    }

    @Override
    public Adapter createBean(AdapterDefinition adapterConfig) {
        final OpenConfigAdapter adapter = new OpenConfigAdapter(adapterConfig, getTelemetryRegistry().getMetricRegistry());
        adapter.setCollectionAgentFactory(getCollectionAgentFactory());
        adapter.setInterfaceToNodeCache(getInterfaceToNodeCache());
        adapter.setNodeDao(getNodeDao());
        adapter.setTransactionTemplate(getTransactionTemplate());
        adapter.setFilterDao(getFilterDao());
        adapter.setPersisterFactory(getPersisterFactory());
        adapter.setThresholdingService(getThresholdingService());
        adapter.setBundleContext(getBundleContext());
        return adapter;
    }
}
