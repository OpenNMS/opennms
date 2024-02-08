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
