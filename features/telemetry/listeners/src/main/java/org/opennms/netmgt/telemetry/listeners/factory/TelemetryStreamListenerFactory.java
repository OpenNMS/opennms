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

import org.opennms.features.openconfig.api.TelemetryClientFactory;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.ListenerFactory;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.api.ListenerDefinition;
import org.opennms.netmgt.telemetry.listeners.TelemetryStreamListener;
import org.opennms.netmgt.telemetry.listeners.UdpParser;

public class TelemetryStreamListenerFactory implements ListenerFactory {

    private final TelemetryRegistry telemetryRegistry;

    private TelemetryClientFactory telemetryClientFactory;


    public TelemetryStreamListenerFactory(TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = telemetryRegistry;
    }

    @Override
    public Class<? extends Listener> getBeanClass() {
        return TelemetryStreamListener.class;
    }

    @Override
    public Listener createBean(ListenerDefinition listenerDefinition) {
        final List<UdpParser> parsers = listenerDefinition.getParsers().stream()
                .map(p -> telemetryRegistry.getParser(p))
                .filter(p -> p instanceof UdpParser)
                .map(p -> (UdpParser) p).collect(Collectors.toList());
        if (parsers.size() != listenerDefinition.getParsers().size()) {
            throw new IllegalArgumentException("Each parser must be of type UdpParser but was not.");
        }
        return new TelemetryStreamListener(listenerDefinition.getName(),
                parsers,
                telemetryRegistry.getMetricRegistry(),
                telemetryClientFactory);
    }

    public void setTelemetryClientFactory(TelemetryClientFactory telemetryClientFactory) {
        this.telemetryClientFactory = telemetryClientFactory;
    }
}
