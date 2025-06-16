/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
                public String getFullName() {
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
