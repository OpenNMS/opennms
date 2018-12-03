/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.commands;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;

@Command(scope = "rpc", name = "stress", description="Generates RPC requests against the Echo module")
@Service
public class StressCommand implements Action {

    private static final Integer MAX_BUFFER_SIZE = 500000;

    @Reference
    public RpcClientFactory rpcClientFactory;

    @Option(name = "-l", aliases = "--location", description = "Location")
    String location = null;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String systemId = null;

    @Option(name = "-t", aliases = "--ttl", description = "Time to live (miliseconds)")
    Long ttlInMs;

    @Option(name = "-d", aliases = "--delay", description = "Response delay (miliseconds)")
    Long delay;

    @Option (name="-ms", aliases = "--message-size", description="Message size (number of charaters)")
    int messageSize = 1024;

    @Option (name="-f", aliases = "--throw-exception", description="Throw ")
    boolean shouldThrow = false;

    @Option (name="-c", aliases = "--count", description="Number of requests")
    int count = 1;

    @Override
    public Object execute() throws Exception {
        // Create the client
        final RpcClient<EchoRequest, EchoResponse> client = rpcClientFactory.getClient(EchoRpcModule.INSTANCE);

        // Create metrics to capture the results
        final MetricRegistry metrics = new MetricRegistry();
        final Histogram responseTimes = metrics.histogram("response-times");
        final Counter successes = metrics.counter("successes");
        final Counter failures = metrics.counter("failures");

        // Build and issue the requests
        System.out.printf("Executing %d requests.\n", count);
        final CountDownLatch doneSignal = new CountDownLatch(count);
        
        long beforeExec = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            client.execute(buildRequest(messageSize)).whenComplete((r,e) -> {
                if (e != null) {
                    failures.inc();
                } else {
                    responseTimes.update(System.currentTimeMillis() - r.getId());
                    successes.inc();
                }
                doneSignal.countDown();
            });
        }
        long afterExec = System.currentTimeMillis();

        // Wait for the responses...
        System.out.printf("Waiting for responses.\n");
        while (true) {
            try {
                if (doneSignal.await(1, TimeUnit.SECONDS)) {
                    // Done!
                    System.out.printf("\nDone!\n");
                    break;
                }
            } catch (InterruptedException e) {
                System.out.println("\nInterrupted!");
                break;
            }
            System.out.print(".");
            System.out.flush();
        }
        long afterResponse = System.currentTimeMillis();

        System.out.println();
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.MILLISECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.report();
        reporter.close();
        System.out.printf("Total miliseconds elapsed: %d\n", afterResponse - beforeExec);
        System.out.printf("Miliseconds spent generating requests: %d\n", afterExec - beforeExec);
        System.out.printf("Miliseconds spent waiting for responses: %d\n", afterResponse - afterExec);

        return null;
    }

    private EchoRequest buildRequest(Integer messageSize) {
        final EchoRequest request = new EchoRequest();
        request.setId(System.currentTimeMillis());
        String message = Strings.repeat("*", messageSize);
        if(messageSize > MAX_BUFFER_SIZE) {
            request.setBody(message);
        } else {
            request.setMessage(message);
        }
        request.setLocation(location);
        request.setSystemId(systemId);
        request.setTimeToLiveMs(ttlInMs);
        request.setDelay(delay);
        request.shouldThrow(shouldThrow);
        return request;
    }
}
