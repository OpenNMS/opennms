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

package org.opennms.netmgt.telemetry.protocols.registry.impl;

import java.util.Map;
import java.util.ServiceLoader;

import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.adapter.AdapterFactory;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;

/**
 * Maintains the list of available telemtryd adapters, aggregating
 * those expose the the service loader and via the OSGi registry.
 *
 * @author chandrag
 * @author jwhite
 */
public class TelemetryAdapterRegistryImpl extends TelemetryServiceRegistryImpl<AdapterFactory, AdapterDefinition, Adapter> {

    public TelemetryAdapterRegistryImpl() {
        this(ServiceLookupBuilder.GRACE_PERIOD_MS, ServiceLookupBuilder.WAIT_PERIOD_MS, ServiceLookupBuilder.LOOKUP_DELAY_MS);
    }

    public TelemetryAdapterRegistryImpl(long gracePeriodMs, long waitPeriodMs, long lookupDelayMs) {
        super(() -> ServiceLoader.load(AdapterFactory.class), gracePeriodMs, waitPeriodMs, lookupDelayMs);
    }

    @Override
    public synchronized void onBind(AdapterFactory adapterFactory, Map properties) {
        super.onBind(adapterFactory, properties);
    }

    @Override
    public synchronized void onUnbind(AdapterFactory adapterFactory, Map properties) {
        super.onUnbind(adapterFactory, properties);
    }
}
