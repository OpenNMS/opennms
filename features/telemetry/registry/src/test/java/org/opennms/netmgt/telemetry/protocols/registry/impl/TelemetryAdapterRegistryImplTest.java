/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertNull;
import static org.opennms.core.soa.lookup.BlockingServiceLookupTest.verifyConsiderPeriods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.adapter.AdapterFactory;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.PackageDefinition;

public class TelemetryAdapterRegistryImplTest {

    // We just need the type of the adapter
    private static class DummyAdapterFactory implements AdapterFactory {
        @Override public Class<DummyAdapter> getBeanClass() { return DummyAdapter.class; }
        @Override public DummyAdapter createBean(AdapterDefinition adapterConfig) { return new DummyAdapter(); }
    }

    // Dummy Adapter implementation
    private static class DummyAdapter implements Adapter {
        @Override
        public void setConfig(AdapterDefinition adapterConfig) {}
        @Override public void handleMessageLog(TelemetryMessageLog messageLog) { }
        @Override public void destroy() { }
    }

    // Here we verify that when in grace period, we retry fetching
    // the service until we successfully get it or the grace period failed
    @Test
    public void verifyGracePeriod() throws InterruptedException, ExecutionException, TimeoutException {
        final long lookupDelay = 250;
        final long gracePeriod = 5000; // How long do we try?
        final long initialDelay = 1000; // Delay before service is made available
        final TelemetryAdapterRegistryImpl registry = new TelemetryAdapterRegistryImpl(gracePeriod, 500, lookupDelay);

        verifyConsiderPeriods(registry, () -> DummyAdapter.class.getCanonicalName(), () -> registry.onBind(new DummyAdapterFactory(), new HashMap()), initialDelay, lookupDelay);
    }

    // Here we verify that when the grace period has passed, we try at least until the waitPeriodMs has passed
    @Test
    public void verifyAlwaysWaits() throws InterruptedException, ExecutionException, TimeoutException {
        final long lookupDelay = 250;
        final long gracePeriod = 0;
        final long initialDelay = 1000; // Delay before service is made available
        final long waitPeriod = 5000;
        final TelemetryAdapterRegistryImpl registry = new TelemetryAdapterRegistryImpl(gracePeriod, waitPeriod, lookupDelay);

        verifyConsiderPeriods(registry, () -> DummyAdapter.class.getCanonicalName(), () -> registry.onBind(new DummyAdapterFactory(), new HashMap()), initialDelay, lookupDelay);
    }

    // Verifies that after a certain time, we bail even if the service is not yet available
    @Test
    public void verifyTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        final long lookupDelay = 250;
        final long gracePeriod = 2000;
        final long waitPeriod = 500;
        final TelemetryAdapterRegistryImpl registry = new TelemetryAdapterRegistryImpl(gracePeriod, waitPeriod, lookupDelay);

        final CompletableFuture<Adapter> future = new CompletableFuture();
        CompletableFuture.runAsync(() -> {
            final Adapter adapter = registry.getService(new AdapterDefinition() {
                @Override
                public String getName() {
                    return DummyAdapter.class.getName();
                }

                @Override
                public String getClassName() {
                    return DummyAdapter.class.getName();
                }

                @Override
                public List<? extends PackageDefinition> getPackages() {
                    return null;
                }

                @Override
                public Map<String, String> getParameterMap() {
                    return null;
                }
            });
            future.complete(adapter);
        });

        // Wait for the future to complete
        Adapter adapter = future.get(gracePeriod * 2, TimeUnit.MILLISECONDS);
        assertNull(adapter);
    }
}
