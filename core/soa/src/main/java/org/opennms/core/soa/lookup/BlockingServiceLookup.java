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

import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to ensure that during startup a service can be fetched even if it is not yet available.
 *
 */
public class BlockingServiceLookup<C, F> implements ServiceLookup<C, F> {

    private static final Logger LOG = LoggerFactory.getLogger(BlockingServiceLookup.class);

    private final ServiceLookup<C, F> delegate;
    private long gracePeriodInMs;
    private Supplier<Long> uptimeSupplier;
    private long lookupDelayMs;
    private long waitTimeMs;

    protected BlockingServiceLookup(ServiceLookup<C, F> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public <T> T lookup(C criteria, F filter) {
        Objects.requireNonNull(criteria);

        // Lookup
        T service = delegate.lookup(criteria, filter);
        if (service != null) {
            return service;
        }

        // A service matching the filter is not currently available.
        // Wait until the system has finished starting up (uptime >= grace period)
        // while ensuring we've waited for at least WAIT_PERIOD_MS before aborting the search.
        final long waitUntil = System.currentTimeMillis() + this.waitTimeMs;
        while (uptimeSupplier.get() < this.gracePeriodInMs
                || System.currentTimeMillis() < waitUntil) {
            try {
                Thread.sleep(this.lookupDelayMs);
            } catch (InterruptedException e) {
                LOG.error("Interrupted while waiting for service with search criteria " + criteria + " to become available in the service registry. Aborting.");
                return null;
            }
            service = delegate.lookup(criteria, filter);
            if (service != null) {
                return service;
            }
        }

        // Couldn't find a service within the defined time
        return null;
    }

    void setGracePeriodInMs(long gracePeriodInMs) {
        this.gracePeriodInMs = gracePeriodInMs;
    }

    void setUptimeSupplier(Supplier<Long> uptimeSupplier) {
        this.uptimeSupplier = uptimeSupplier;
    }

    void setLookupDelayMs(long lookupDelayMs) {
        this.lookupDelayMs = lookupDelayMs;
    }

    public void setWaitTimeMs(long waitTimeMs) {
        this.waitTimeMs = waitTimeMs;
    }
}
