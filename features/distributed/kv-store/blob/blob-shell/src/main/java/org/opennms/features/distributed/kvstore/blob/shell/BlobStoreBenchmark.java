/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

@Command(scope = "opennms-kv-blob", name = "benchmark", description = "Benchmark the blob store's throughput")
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
