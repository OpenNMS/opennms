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

package org.opennms.netmgt.dnsresolver.shell;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.net.InetAddresses;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Used to stress test (reverse) DNS lookups.
 *
 * @author jwhite
 */
@Command(scope = "opennms-dns", name = "stress", description="Stress the DNS lookups")
@Service
public class StressCommand implements Action {

    @Reference
    public DnsResolver dnsResolver;

    @Option(name="-l", aliases="--lps", description="Lookups per seconds to generate per thread.")
    int lookupsPerSecondPerThread = 300;

    @Option(name="-t", aliases="--threads", description="Number of threads used to generated lookups.")
    int numberOfThreads = 1;

    @Option(name="-s", aliases="--seconds", description="Number of seconds to run")
    int durationInSeconds = 60;

    @Option(name="-r", aliases="--report", description="Number of seconds after which the report should be generated")
    int reportIntervalInSeconds = 5;

    private AtomicInteger nextIpAddress = new AtomicInteger(16843009); // Start at 1.1.1.1

    private final MetricRegistry metrics = new MetricRegistry();

    private final Meter lookups = metrics.meter("lookups");
    private final Meter responseSuccess = metrics.meter("response-success");
    private final Meter responseFailed = metrics.meter("response-failed");

    private class DNSLookupGenerator implements Runnable {
        private final Set<CompletableFuture<Optional<String>>> pendingFutures = new HashSet<>();

        @Override
        public void run() {
            final RateLimiter rateLimiter = RateLimiter.create(lookupsPerSecondPerThread);
            while (true) {
                rateLimiter.acquire(1);
                final InetAddress addr = InetAddresses.fromInteger(nextIpAddress.incrementAndGet());
                final CompletableFuture<Optional<String>> future = dnsResolver.reverseLookup(addr);
                synchronized (pendingFutures) {
                    pendingFutures.add(future);
                }
                future.whenComplete((hostnameFromDns, ex) -> {
                    synchronized (pendingFutures) {
                        pendingFutures.remove(future);
                    }
                    if (ex == null) {
                        responseSuccess.mark();
                    } else {
                        responseFailed.mark();
                    }
                });
                lookups.mark();
                if (Thread.interrupted()) {
                    break;
                }
            }

            // Copy the list of pending futures - so they don't change on us
            final List<CompletableFuture<Optional<String>>> futuresToWaitFor;
            synchronized (pendingFutures) {
                futuresToWaitFor = new LinkedList<>(pendingFutures);
            }
            System.out.printf("Waiting for %d pending requests...\n", futuresToWaitFor.size());

            try {
                CompletableFuture.allOf(futuresToWaitFor.toArray(new CompletableFuture[]{})).get(1, TimeUnit.MINUTES);
                System.out.println("Pending requests completed.");
            } catch (TimeoutException e) {
                System.out.println("Requests did not complete in time.");
            } catch (ExecutionException e) {
                // One of the futures failed, but that's OK, since they must all have finished
                System.out.println("Pending requests completed.");
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for pending requests.");
            }
        }
    }

    @Override
    public Object execute() {
        // Apply sane lower bounds to all of the configurable options
        lookupsPerSecondPerThread = Math.max(1, lookupsPerSecondPerThread);
        numberOfThreads = Math.max(1, numberOfThreads);
        durationInSeconds = Math.max(1, durationInSeconds);
        reportIntervalInSeconds = Math.max(1, reportIntervalInSeconds);

        // Display the effective settings and rates
        double lookupsPerSecond = (double)lookupsPerSecondPerThread * (double)numberOfThreads;
        System.out.printf("Generating %d DNS lookups per second accross %d threads for %d seconds\n",
                lookupsPerSecondPerThread, numberOfThreads, durationInSeconds);
        System.out.printf("Which will yield an effective\n");
        System.out.printf("\t %.2f lookups per second\n", lookupsPerSecond);
        System.out.printf("\t %.2f total lookups\n", lookupsPerSecond * durationInSeconds);

        // Setup the reporter
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        // Setup the executor
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("DNS Lookup Generator #%d")
                .build();
        final ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads, threadFactory);

        System.out.println("Starting.");
        try {
            reporter.start(reportIntervalInSeconds, TimeUnit.SECONDS);
            for (int i = 0; i < numberOfThreads; i++) {
                final DNSLookupGenerator generator = new DNSLookupGenerator();
                executor.execute(generator);
            }
            System.out.println("Started.");

            // Wait until we timeout or get interrupted
            try {
                Thread.sleep(SECONDS.toMillis(durationInSeconds));
            } catch (InterruptedException e) { }

            // Stop!
            try {
                System.out.println("Stopping.");
                executor.shutdownNow();
                if (!executor.awaitTermination(2, TimeUnit.MINUTES)) {
                    System.err.println("The threads did not stop in time.");
                } else {
                    System.out.println("Stopped.");
                }
            } catch (InterruptedException e) { }
        } finally {
            // Make sure we always stop the reporter
            reporter.stop();
        }

        // And display one last report...
        reporter.report();
        return null;
    }
}
