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

package org.opennms.netmgt.telemetry.protocols.sflow.adapter;

import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractCollectionAdapterFactory;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class SFlowTelemetryAdapterFactory extends AbstractCollectionAdapterFactory {

    public SFlowTelemetryAdapterFactory() {
        super(null);
    }

    public SFlowTelemetryAdapterFactory(BundleContext bundleContext) {
        super(bundleContext);
    }

    @Override
    public Class<? extends Adapter> getBeanClass() {
        return SFlowTelemetryAdapter.class;
    }

    @Override
    public Adapter createBean(final AdapterDefinition adapterConfig) {
        final SFlowTelemetryAdapter adapter = new SFlowTelemetryAdapter();
        adapter.setConfig(adapterConfig);
        adapter.setCollectionAgentFactory(getCollectionAgentFactory());
        adapter.setPersisterFactory(getPersisterFactory());
        adapter.setFilterDao(getFilterDao());
        adapter.setPersisterFactory(getPersisterFactory());
        adapter.setInterfaceToNodeCache(getInterfaceToNodeCache());
        adapter.setBundleContext(getBundleContext());

        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(adapter);
        wrapper.setPropertyValues(adapterConfig.getParameterMap());

        return adapter;
    }
}
