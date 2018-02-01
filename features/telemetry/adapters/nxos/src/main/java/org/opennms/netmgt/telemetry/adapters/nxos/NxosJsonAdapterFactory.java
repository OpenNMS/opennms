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

package org.opennms.netmgt.telemetry.adapters.nxos;

import java.util.Map;

import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.adapters.collection.AbstractCollectionAdapterFactory;
import org.opennms.netmgt.telemetry.config.api.Protocol;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * A factory for creating NxosJsonAdapter objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class NxosJsonAdapterFactory extends AbstractCollectionAdapterFactory {

    /**
     * Instantiates a new NXOS JSON adapter factory.
     *
     * @param bundleContext the bundle context
     */
    public NxosJsonAdapterFactory(BundleContext bundleContext) {
        super(bundleContext);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.telemetry.adapters.api.AdapterFactory#getAdapterClass()
     */
    @Override
    public Class<? extends Adapter> getAdapterClass() {
        return NxosJsonAdapter.class;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.telemetry.adapters.factory.api.AdapterFactory#createAdapter(org.opennms.netmgt.telemetry.config.api.Protocol, java.util.Map)
     */
    @Override
    public Adapter createAdapter(Protocol protocol, Map<String, String> properties) {
        final NxosJsonAdapter adapter = new NxosJsonAdapter();
        adapter.setProtocol(protocol);
        adapter.setCollectionAgentFactory(getCollectionAgentFactory());
        adapter.setInterfaceToNodeCache(getInterfaceToNodeCache());
        adapter.setNodeDao(getNodeDao());
        adapter.setTransactionTemplate(getTransactionTemplate());
        adapter.setFilterDao(getFilterDao());
        adapter.setPersisterFactory(getPersisterFactory());
        adapter.setBundleContext(getBundleContext());

        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(adapter);
        wrapper.setPropertyValues(properties);
        return adapter;
    }

}
