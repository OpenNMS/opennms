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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.distributed.kvstore.api.BlobStore;

import com.google.common.base.Stopwatch;

@Command(scope = "opennms-kv-blob", name = "benchmark-read", description = "Benchmark the blob store's read " +
        "throughput")
@Service
public class BlobStoreReadBenchmark implements Action {
    @Reference
    private BlobStore blobStore;

    @Argument(index = 0, description = "The payload size in bytes", required = true)
    private int payloadSize;

    @Argument(index = 1, description = "The number of records to read", required = true)
    private int numberOfReads;

    @Argument(index = 2, description = "Whether or not to read just the timestamp", required = true)
    private boolean readJustTimestamp = false;

    @Argument(index = 3, description = "Whether or not to use async")
    private boolean async = false;

    private final String CONTEXT = "benchmark";

    private final String KEY = "read-test";

    @Override
    public Object execute() throws ExecutionException, InterruptedException {
        // Put a record to be read
        blobStore.put(KEY, new byte[payloadSize], CONTEXT, (int) TimeUnit.SECONDS.convert(1, TimeUnit.HOURS));

        if (async) {
            benchmarkAsync();
        } else {
            benchmark();
        }

        return null;
    }

    private void benchmark() {
        System.out.println("Benchmarking read performance...");

        Stopwatch stopwatch = Stopwatch.createStarted();

        for (int i = 0; i < numberOfReads; i++) {
            if (readJustTimestamp) {
                blobStore.getLastUpdated(KEY, CONTEXT);
            } else {
                blobStore.get(KEY, CONTEXT);
            }
        }

        long elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        stopwatch.stop();
        displayResults(elapsedTime);
    }

    private void benchmarkAsync() throws ExecutionException, InterruptedException {
        System.out.println("Benchmarking reads async...");

        Stopwatch stopwatch = Stopwatch.createStarted();

        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfReads; i++) {
            if (readJustTimestamp) {
                futures.add(blobStore.getLastUpdatedAsync(KEY, CONTEXT));
            } else {
                futures.add(blobStore.getAsync(KEY, CONTEXT));
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        long elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        stopwatch.stop();
        displayResults(elapsedTime);
    }

    private void displayResults(long elapsedTimeMillis) {
        double elapsed = (double) elapsedTimeMillis;
        double numberOfReads = this.numberOfReads;
        double payloadSize = this.payloadSize;

        double averageLatency = elapsed / numberOfReads;
        System.out.println(String.format("Read %d records in %d milliseconds", this.numberOfReads, elapsedTimeMillis));
        System.out.println(String.format("Average read latency: %.2f milliseconds", averageLatency));
        if (!readJustTimestamp) {
            double throughPut = ((payloadSize * numberOfReads) / 1024.0) / (elapsed / 1000.0);
            System.out.println(String.format("Average read throughput: %.2f KB/s", throughPut));
        }
    }
}
