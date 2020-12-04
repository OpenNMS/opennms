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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractCollectionAdapterFactory;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

public class BmpTelemetryAdapterFactory extends AbstractCollectionAdapterFactory {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public BmpTelemetryAdapterFactory() {
        super(null);
    }

    public BmpTelemetryAdapterFactory(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public Class<? extends Adapter> getBeanClass() {
        return BmpTelemetryAdapter.class;
    }

    @Override
    public Adapter createBean(final AdapterDefinition adapterConfig) {
        final BmpTelemetryAdapter adapter = new BmpTelemetryAdapter(adapterConfig, this.getTelemetryRegistry().getMetricRegistry(), nodeDao, transactionTemplate);
        adapter.setCollectionAgentFactory(this.getCollectionAgentFactory());
        adapter.setPersisterFactory(this.getPersisterFactory());
        adapter.setFilterDao(this.getFilterDao());
        adapter.setPersisterFactory(this.getPersisterFactory());
        adapter.setInterfaceToNodeCache(this.getInterfaceToNodeCache());
        adapter.setThresholdingService(this.getThresholdingService());
        adapter.setBundleContext(this.getBundleContext());

        return adapter;
    }

}
