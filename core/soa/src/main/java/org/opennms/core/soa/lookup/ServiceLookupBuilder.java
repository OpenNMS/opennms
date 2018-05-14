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

import org.opennms.core.soa.ServiceRegistry;

public class ServiceLookupBuilder {

    public static final long GRACE_PERIOD_MS = Long.getLong("org.opennms.core.soa.lookup.gracePeriodMs", TimeUnit.MINUTES.toMillis(5));

    public static final long WAIT_PERIOD_MS = Long.getLong("org.opennms.core.soa.lookup.gracePeriodMs", TimeUnit.MINUTES.toMillis(1));

    public static final long LOOKUP_DELAY_MS = Long.getLong("org.opennms.core.soa.lookup.lookupDelayMs", TimeUnit.SECONDS.toMillis(5));

    private final ServiceRegistry registry;
    private long gracePeriodInMs;
    private long sleepTimeInMs;
    private Supplier<Long> upTimeSupplier;

    public ServiceLookupBuilder(ServiceRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
    }

    public ServiceLookupBuilder blocking() {
        return blocking(GRACE_PERIOD_MS, LOOKUP_DELAY_MS, () -> ManagementFactory.getRuntimeMXBean().getUptime());
    }

    public ServiceLookupBuilder blocking(long gracePeriodInMs, long sleepTimeInMs, Supplier<Long> upTimeSupplier) {
        Objects.requireNonNull(upTimeSupplier);
        this.gracePeriodInMs = gracePeriodInMs;
        this.sleepTimeInMs = sleepTimeInMs;
        this.upTimeSupplier = Objects.requireNonNull(upTimeSupplier);
        return this;
    }

    public ServiceLookup build() {
        if (this.upTimeSupplier != null) {
            BlockingServiceLookup lookup = new BlockingServiceLookup(this.registry);
            lookup.setGracePeriodInMs(gracePeriodInMs);
            lookup.setLookupDelayMs(sleepTimeInMs);
            lookup.setUptimeSupplier(upTimeSupplier);
            return lookup;
        }
        return new SimpleServiceLookup(this.registry);
    }

}
