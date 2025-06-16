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
package org.opennms.netmgt.telemetry.protocols.bmp.parser;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.api.receiver.ParserFactory;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;

public class BmpParserFactory implements ParserFactory {
    public static final String MAX_CONCURRENT_CALLS_KEY = "bulkhead.maxConcurrentCalls";
    public static final long DEFAULT_MAX_CONCURRENT_CALLS = 1000;

    public static final String MAX_WAIT_DURATION_MS_KEY = "bulkhead.maxWaitDurationMs";
    public static final long DEFAULT_MAX_WAIT_DURATION_MS = TimeUnit.MINUTES.toMillis(5);

    private final TelemetryRegistry telemetryRegistry;

    private final DnsResolver dnsResolver;

    public BmpParserFactory(final TelemetryRegistry telemetryRegistry,
                            final DnsResolver dnsResolver) {
        this.telemetryRegistry = Objects.requireNonNull(telemetryRegistry);
        this.dnsResolver = Objects.requireNonNull(dnsResolver);
    }

    @Override
    public Class<? extends Parser> getBeanClass() {
        return BmpParser.class;
    }

    @Override
    public Parser createBean(ParserDefinition parserDefinition) {
        final AsyncDispatcher<TelemetryMessage> dispatcher = this.telemetryRegistry.getDispatcher(parserDefinition.getQueueName());
        final Bulkhead bulkhead = createBulkhead(parserDefinition);

        return new BmpParser(parserDefinition.getFullName(),
                             dispatcher,
                             this.dnsResolver,
                             bulkhead,
                             this.telemetryRegistry.getMetricRegistry());
    }

    private static Bulkhead createBulkhead(ParserDefinition parserDefinition) {
        // Build the bulkhead configuration from the parameter map
        final Map<String, String> parameters = parserDefinition.getParameterMap();
        final long maxConcurrentCalls = Optional.ofNullable(parameters.remove(MAX_CONCURRENT_CALLS_KEY))
                                                .map(Long::parseLong)
                                                .orElse(DEFAULT_MAX_CONCURRENT_CALLS);
        final long maxWaitDurationMs = Optional.ofNullable(parameters.remove(MAX_WAIT_DURATION_MS_KEY))
                                                .map(Long::parseLong)
                                                .orElse(DEFAULT_MAX_WAIT_DURATION_MS);

        final BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls((int) maxConcurrentCalls)
                .maxWaitDuration(Duration.ofMillis(maxWaitDurationMs))
                .build();

        return Bulkhead.of("BmpParser-" + parserDefinition.getFullName(), bulkheadConfig);
    }
}
