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

package org.opennms.core.soa.lookup;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.Test;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.DefaultServiceRegistry;

public class BlockingServiceLookupTest {

    // Verifies that after a certain time, we bail even if the service is not yet available
    @Test
    public void verifyTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        final long lookupDelay = 250;
        final long gracePeriod = 2000;
        final long waitPeriod = 0;
        final ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        final ServiceLookup<Class<?>, String> serviceLookup = new ServiceLookupBuilder(new ServiceRegistryLookup(serviceRegistry))
                .blocking(gracePeriod, lookupDelay, waitPeriod)
                .build();

        final CompletableFuture<Date> future = new CompletableFuture();
        CompletableFuture.runAsync(() -> {
            final Date date = serviceLookup.lookup(Date.class, null);
            future.complete(date);
        });

        // Wait for the future to complete
        Date date = future.get(gracePeriod * 2, TimeUnit.MILLISECONDS);
        assertNull(date);
    }


    // Here we verify that when in grace period, we retry fetching
    // the service until we successfully get it or the grace period failed
    @Test
    public void verifyGracePeriod() throws InterruptedException, ExecutionException, TimeoutException {
        final long lookupDelay = 250;
        final long waitTime = 500;
        final long gracePeriod = 5000; // How long do we try?
        final long initialDelay = 1000; // Delay before service is made available

        verifyConsiderPeriods(lookupDelay, waitTime, gracePeriod, initialDelay);
    }

    // Here we verify that when the grace period has passed, we try at least until the waitPeriodMs has passed
    @Test
    public void verifyAlwaysWaits() throws InterruptedException, ExecutionException, TimeoutException {
        final long lookupDelay = 250;
        final long gracePeriod = 0;
        final long initialDelay = 1000; // Delay before service is made available
        final long waitPeriod = 5000;
        verifyConsiderPeriods(lookupDelay, waitPeriod, gracePeriod, initialDelay);
    }

    private static void verifyConsiderPeriods(long lookupDelay, long waitTime, long gracePeriod, long initialDelay) throws InterruptedException, ExecutionException, TimeoutException {
        final ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        final ServiceLookup<Class<?>, String> serviceLookup = new ServiceLookupBuilder(new ServiceRegistryLookup(serviceRegistry))
                .blocking(gracePeriod, lookupDelay, waitTime)
                .build();

        verifyConsiderPeriods(serviceLookup, () -> Date.class, () -> serviceRegistry.register(new Date(), Date.class), initialDelay, lookupDelay);
    }

    public static <T, C, F> void verifyConsiderPeriods(ServiceLookup<C, F> serviceLookup, Supplier<C> searchCriteriaSupplier, Runnable registerCallback, long initialDelay, long lookupDelay) throws InterruptedException, ExecutionException, TimeoutException {
        final C lookupCriteria = searchCriteriaSupplier.get();

        final CompletableFuture<Long> future = new CompletableFuture();
        CompletableFuture.runAsync(() -> {
            final long start = System.currentTimeMillis();
            final T object = serviceLookup.lookup(lookupCriteria, null);
            assertNotNull(object);
            final long took = System.currentTimeMillis() - start;
            future.complete(took);
        });

        // Wait before making it available
        new Thread(() -> {
            try {
                Thread.sleep(initialDelay);
                registerCallback.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        // Wait for the future to complete
        long took = future.get((initialDelay + lookupDelay) * 2, TimeUnit.MILLISECONDS);
        assertThat(took, allOf(greaterThanOrEqualTo(initialDelay), lessThanOrEqualTo(initialDelay + lookupDelay * 2)));

    }
}