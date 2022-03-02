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
