/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.factory;

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.openconfig.api.OpenConfigClientFactory;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.ListenerFactory;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.api.ListenerDefinition;
import org.opennms.netmgt.telemetry.listeners.StreamParser;
import org.opennms.netmgt.telemetry.listeners.TelemetryStreamListener;

public class TelemetryStreamListenerFactory implements ListenerFactory {

    private final TelemetryRegistry telemetryRegistry;

    private final EventSubscriptionService eventSubscriptionService;

    private OpenConfigClientFactory openConfigClientFactory;

    public TelemetryStreamListenerFactory(TelemetryRegistry telemetryRegistry, EventSubscriptionService eventSubscriptionService) {
        this.telemetryRegistry = telemetryRegistry;
        this.eventSubscriptionService = eventSubscriptionService;
    }

    @Override
    public Class<? extends Listener> getBeanClass() {
        return TelemetryStreamListener.class;
    }

    @Override
    public Listener createBean(ListenerDefinition listenerDefinition) {
        final List<StreamParser> parsers = listenerDefinition.getParsers().stream()
                .map(p -> telemetryRegistry.getParser(p))
                .filter(p -> p instanceof StreamParser)
                .map(p -> (StreamParser) p).collect(Collectors.toList());
        if (parsers.size() != listenerDefinition.getParsers().size()) {
            throw new IllegalArgumentException("Each parser must be of type StreamParser but was not.");
        }
        // Expects only one parser for stream listener.
        StreamParser parser = parsers.get(0);
        return new TelemetryStreamListener(listenerDefinition.getName(),
                parser,
                openConfigClientFactory,
                eventSubscriptionService,
                telemetryRegistry.getMetricRegistry());
    }

    public void setOpenConfigClientFactory(OpenConfigClientFactory openConfigClientFactory) {
        this.openConfigClientFactory = openConfigClientFactory;
    }
}
