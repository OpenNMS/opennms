/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.concurrent;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Test;

public class FutureUtilsTest {

    @Test
    public void traverses() throws Exception {
        var is1 = Arrays.asList(1, 2, 3, 4, 5);
        var f = FutureUtils.traverse(
                is1,
                i -> FutureUtils.completionStageWithTimeoutException(
                        () -> i,
                        Duration.ofMinutes(1),
                        () -> new Exception("Timeout"),
                        ForkJoinPool.commonPool()
                )
        );
        var is2 = f.toCompletableFuture().get();
        assertThat(is2, Matchers.is(is1));
    }

    @Test
    public void defaultValueOnTimeout() throws Exception {

        var is1 = Arrays.asList(1, 2, 3, 4, 5);

        // check that all started computations get interrupted
        // -> consider that some computation may get cancelled even before they started
        var started = new AtomicInteger();
        var interrupted = new AtomicInteger();

        var f = FutureUtils.traverse(
                is1,
                i -> FutureUtils.completionStageWithDefaultOnTimeout(
                        () -> {
                            started.incrementAndGet();
                            try {
                                while (true) Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                interrupted.incrementAndGet();
                                throw e;
                            }
                        },
                        Duration.ofSeconds(1), // after 1 second the default value is used
                        () -> 0,
                        // we can not use the ForkJoinPool.commonPool() here because cancellation causes no interruptions
                        Executors.newCachedThreadPool()
                )
        );
        var is2 = f.toCompletableFuture().get(10, TimeUnit.SECONDS);
        var is3 = is1.stream().map(i -> 0).collect(Collectors.toList());

        assertThat(is2, Matchers.is(is3));

        await().untilAsserted(() -> assertThat(interrupted.get(), Matchers.is(started.get())));
    }

    @Test(expected = TimeoutException.class)
    public void exceptionOnTimeout() throws Throwable {
        var f = FutureUtils.completionStageWithTimeoutException(
                () -> { while(true) Thread.sleep(1000); },
                Duration.ofSeconds(1),
                () -> new TimeoutException(),
                // we can not use the ForkJoinPool.commonPool() here because cancellation causes no interruptions
                Executors.newCachedThreadPool()
        );
        // the future will complete with a TimeoutException
        // -> use that exception as the result of another future, get it, and then throw it
        throw f.handle((r, t) -> t).toCompletableFuture().get();
    }

}
