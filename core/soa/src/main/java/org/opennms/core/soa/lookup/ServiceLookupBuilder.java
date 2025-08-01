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
package org.opennms.core.soa.lookup;

import java.lang.management.ManagementFactory;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.opennms.core.sysprops.SystemProperties;

public class ServiceLookupBuilder<C, F> {

    public static final long GRACE_PERIOD_MS = SystemProperties.getLong("org.opennms.core.soa.lookup.gracePeriodMs", TimeUnit.MINUTES.toMillis(5));

    public static final long WAIT_PERIOD_MS = SystemProperties.getLong("org.opennms.core.soa.lookup.waitPeriodMs",
                                              SystemProperties.getLong("org.opennms.core.soa.lookup.gracePeriodMs",
                                              TimeUnit.MINUTES.toMillis(1)));

    public static final long BLOCKING_WAIT_PERIOD_MS = SystemProperties.getLong("org.opennms.core.soa.lookup.waitPeriodMs",
                                                       SystemProperties.getLong("org.opennms.core.soa.lookup.gracePeriodMs",
                                                       TimeUnit.MINUTES.toMillis(5)));

    public static final long LOOKUP_DELAY_MS = SystemProperties.getLong("org.opennms.core.soa.lookup.lookupDelayMs", TimeUnit.SECONDS.toMillis(5));

    private final ServiceLookup<C, F> serviceProvider;
    private long gracePeriodInMs;
    private long sleepTimeInMs;
    private long waitTimeMs;
    private Supplier<Long> upTimeSupplier;

    public ServiceLookupBuilder(ServiceLookup<C, F> serviceProvider) {
        this.serviceProvider = Objects.requireNonNull(serviceProvider);
    }

    public ServiceLookupBuilder blocking() {
        return blocking(GRACE_PERIOD_MS, LOOKUP_DELAY_MS, BLOCKING_WAIT_PERIOD_MS);
    }

    public ServiceLookupBuilder blocking(long gracePeriodInMs, long sleepTimeInMs, long waitTimeMs) {
        return blocking(gracePeriodInMs, sleepTimeInMs, waitTimeMs, () -> ManagementFactory.getRuntimeMXBean().getUptime());
    }

    public ServiceLookupBuilder blocking(long gracePeriodInMs, long sleepTimeInMs, long waitTimeMs, Supplier<Long> upTimeSupplier) {
        Objects.requireNonNull(upTimeSupplier);
        this.gracePeriodInMs = gracePeriodInMs;
        this.sleepTimeInMs = sleepTimeInMs;
        this.waitTimeMs = waitTimeMs;
        this.upTimeSupplier = Objects.requireNonNull(upTimeSupplier);
        return this;
    }

    public ServiceLookup<C, F> build() {
        if (this.upTimeSupplier != null) {
            BlockingServiceLookup lookup = new BlockingServiceLookup(serviceProvider);
            lookup.setGracePeriodInMs(gracePeriodInMs);
            lookup.setLookupDelayMs(sleepTimeInMs);
            lookup.setWaitTimeMs(waitTimeMs);
            lookup.setUptimeSupplier(upTimeSupplier);
            return lookup;
        }
        return serviceProvider;
    }

}
