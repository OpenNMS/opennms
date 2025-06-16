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
                LOG.error("Interrupted while waiting for service with search criteria '{}' to become available in the service registry. Aborting.", criteria);
                return null;
            }
            service = delegate.lookup(criteria, filter);
            if (service != null) {
                return service;
            }
        }

        // Couldn't find a service within the defined time
        LOG.error("Timed out while waiting for service with search criteria '{}' to become available in the service registry. Aborting.", criteria);
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
