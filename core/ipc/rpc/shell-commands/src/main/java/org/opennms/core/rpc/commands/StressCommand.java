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

@Command(scope = "opennms", name = "stress-rpc", description="Generates RPC requests against the Echo module")
@Service
public class StressCommand implements Action {

    @Reference
    private RpcClientFactory rpcClientFactory;

    @Option(name = "-l", aliases = "--location", description = "Location")
    String location = null;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String systemId = null;

    @Option(name = "-t", aliases = "--ttl", description = "Time to live (milliseconds)")
    Long ttlInMs;

    @Option(name = "-d", aliases = "--delay", description = "Response delay (milliseconds)")
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
        System.out.printf("Total milliseconds elapsed: %d\n", afterResponse - beforeExec);
        System.out.printf("Milliseconds spent generating requests: %d\n", afterExec - beforeExec);
        System.out.printf("Milliseconds spent waiting for responses: %d\n", afterResponse - afterExec);

        return null;
    }

    private EchoRequest buildRequest(Integer messageSize) {
        final EchoRequest request = new EchoRequest();
        request.setId(System.currentTimeMillis());
        String message = Strings.repeat("*", messageSize);
        request.setBody(message);
        request.setLocation(location);
        request.setSystemId(systemId);
        request.setTimeToLiveMs(ttlInMs);
        request.setDelay(delay);
        request.shouldThrow(shouldThrow);
        return request;
    }
}
