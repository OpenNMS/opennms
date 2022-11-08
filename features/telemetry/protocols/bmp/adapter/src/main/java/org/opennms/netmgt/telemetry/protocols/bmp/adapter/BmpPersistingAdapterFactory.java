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

import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.BmpMessageHandler;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractAdapterFactory;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BmpPersistingAdapterFactory extends AbstractAdapterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(BmpPersistingAdapterFactory.class);

    private final BmpMessageHandler bmpMessageHandler;

    public BmpPersistingAdapterFactory(BmpMessageHandler bmpMessageHandler) {
        super(null);
        this.bmpMessageHandler = bmpMessageHandler;
    }

    public BmpPersistingAdapterFactory(BundleContext bundleContext, BmpMessageHandler bmpMessageHandler) {
        super(bundleContext);
        this.bmpMessageHandler = bmpMessageHandler;
    }

    @Override
    public Class<? extends Adapter> getBeanClass() {
        return BmpPersistingAdapter.class;
    }

    @Override
    public Adapter createBean(AdapterDefinition adapterConfig) {

        return new BmpPersistingAdapter(adapterConfig,
                this.getTelemetryRegistry().getMetricRegistry(),
                bmpMessageHandler);
    }

}
