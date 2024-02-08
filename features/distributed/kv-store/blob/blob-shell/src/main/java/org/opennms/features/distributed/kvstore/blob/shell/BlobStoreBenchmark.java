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
package org.opennms.features.distributed.kvstore.blob.shell;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.distributed.kvstore.api.BlobStore;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

@Command(scope = "opennms", name = "kv-benchmark-blob", description = "Benchmark the blob store's throughput")
@Service
public class BlobStoreBenchmark implements Action {
    @Reference
    private BlobStore blobStore;

    @Argument(index = 0, description = "The payload size in bytes", required = true)
    private int payloadSize;

    @Argument(index = 1, description = "The number of records", required = true)
    private int numberOfRecords;

    @Option(name = "-t", aliases = "--just-timestamp", description = "Whether or not to read just the timestamp")
    private boolean readJustTimestamp = false;

    @Option(name = "-a", aliases = "--async", description = "Whether or not to use async")
    private boolean async = false;

    private static final String CONTEXT = "benchmark";

    private static final String KEY = "test";

    private final MetricRegistry metrics = new MetricRegistry();

    private byte[] writePayload;
    
    // benchmarking indicated using a single thread here performed better than a pool
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    // Since this benchmark can overwhelm a blobstore's connection pool, we will use a retry to limit the rate we send
    // async requests
    private final Retry asyncRetry = Retry.of("blobStoreBenchmark", RetryConfig.custom()
            .maxAttempts(Integer.MAX_VALUE)
            .waitDuration(Duration.ofMillis(10))
            .build());

    @Override
    public Object execute() throws InterruptedException {
        System.out.println(String.format("BlobStore implementation in use: %s", blobStore.getName()));
        writePayload = new byte[payloadSize];
        StringBuilder throughputResultsBuilder = new StringBuilder();
        throughputResultsBuilder.append(benchmark("write", this::writeAsync, this::write));
        String readThroughput = benchmark("read", this::readAsync, this::read);

        // The read throughput is only really meaningful if we are doing a full fetch of the value
        if (!readJustTimestamp) {
            throughputResultsBuilder.append('\n').append(readThroughput);
        }

        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics).build();
        reporter.report();
        reporter.close();

        System.out.println(throughputResultsBuilder.toString());
        return null;
    }

    private CompletableFuture<?> timeAsyncOperation(Histogram results, Supplier<CompletableFuture<?>> futureSupplier) {
        long start = System.currentTimeMillis();

        return Retry.decorateCompletionStage(asyncRetry, executorService,
                () -> futureSupplier.get()
                        .thenAccept(v ->
                                results.update(System.currentTimeMillis() - start))).get().toCompletableFuture();
    }

    private CompletableFuture<?> writeAsync(String key, Histogram results) {
        return timeAsyncOperation(results, () -> blobStore.putAsync(key, writePayload, CONTEXT,
                (int) TimeUnit.SECONDS.convert(1, TimeUnit.HOURS)));
    }

    private CompletableFuture<?> readAsync(String key, Histogram results) {
        return timeAsyncOperation(results, () -> readJustTimestamp ? blobStore.getLastUpdatedAsync(key, CONTEXT) :
                blobStore.getAsync(key, CONTEXT));
    }

    private void timeOperation(Histogram results, Runnable operation) {
        long start = System.currentTimeMillis();
        operation.run();
        long elapsed = System.currentTimeMillis() - start;
        results.update(elapsed);
    }

    private void write(String key, Histogram results) {
        timeOperation(results, () -> blobStore.put(key, writePayload, CONTEXT, (int) TimeUnit.SECONDS.convert(1,
                TimeUnit.HOURS)));
    }

    private void read(String key, Histogram results) {
        timeOperation(results, readJustTimestamp ? () -> blobStore.getLastUpdated(key, CONTEXT) :
                () -> blobStore.get(key, CONTEXT));
    }

    private String benchmark(String methodType,
                             BiFunction<String, Histogram, CompletableFuture<?>> asyncFunction,
                             BiConsumer<String, Histogram> syncFunction) throws InterruptedException {
        System.out.print(String.format("Benchmarking %s performance...", methodType));

        Histogram results = metrics.histogram(String.format("%s times", methodType));
        AtomicLong totalTime = new AtomicLong(0);

        // Do the benchmark on another thread so we can provide feedback while it is executing
        CompletableFuture<Boolean> benchmarkFuture = CompletableFuture.supplyAsync(() -> {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            long start = System.currentTimeMillis();

            for (int i = 0; i < numberOfRecords; i++) {
                String key = String.format("%s-%d", KEY, i);

                if (this.async) {
                    futures.add(asyncFunction.apply(key, results));
                } else {
                    syncFunction.accept(key, results);
                }
            }

            if (this.async) {
                try {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            totalTime.set(System.currentTimeMillis() - start);

            return true;
        });

        // Give user feedback while the benchmark is running
        while (!benchmarkFuture.getNow(false)) {
            System.out.print('.');
            Thread.sleep(1000);
        }

        System.out.println("done");

        double throughPut = ((payloadSize * numberOfRecords) / 1024.0) / (totalTime.get() / 1000.0);
        return String.format("%s throughput: %.2f KB/s", methodType, throughPut);
    }
}
