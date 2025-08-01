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
@Command(scope = "opennms", name = "stress-dns", description="Stress the DNS lookups")
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
