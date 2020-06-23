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

package org.opennms.core.soa.lookup;

import java.lang.management.ManagementFactory;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.opennms.core.sysprops.SystemProperties;

public class ServiceLookupBuilder<C, F> {

public static final long GRACE_PERIOD_MS = SystemProperties.getLong("org.opennms.core.soa.lookup.gracePeriodMs", TimeUnit.MINUTES.toMillis(5));

    public static final long WAIT_PERIOD_MS = SystemProperties.getLong("org.opennms.core.soa.lookup.gracePeriodMs", TimeUnit.MINUTES.toMillis(1));

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
        return blocking(GRACE_PERIOD_MS, LOOKUP_DELAY_MS, GRACE_PERIOD_MS);
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
