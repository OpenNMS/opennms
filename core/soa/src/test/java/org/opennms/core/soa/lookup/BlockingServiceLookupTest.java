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