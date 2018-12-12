/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.ListenerFactory;
import org.opennms.netmgt.telemetry.config.api.ListenerDefinition;
import org.opennms.netmgt.telemetry.listeners.TcpListener;
import org.opennms.netmgt.telemetry.listeners.TcpParser;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class TcpListenerFactory implements ListenerFactory {

    private final TelemetryRegistry telemetryRegistry;

    public TcpListenerFactory(TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = Objects.requireNonNull(telemetryRegistry);
    }

    @Override
    public Class<? extends Listener> getBeanClass() {
        return TcpListener.class;
    }

    @Override
    public Listener createBean(ListenerDefinition listenerDefinition) {
        // TcpListener only supports one parser at a time
        if (listenerDefinition.getParsers().size() != 1) {
            throw new IllegalArgumentException("The simple TCP listener supports exactly one parser");
        }
        // Ensure each defined parser is of type TcpParser
        final List<TcpParser> parser = listenerDefinition.getParsers().stream()
                .map(p -> telemetryRegistry.getParser(p))
                .filter(p -> p instanceof TcpParser && p != null)
                .map(p -> (TcpParser) p).collect(Collectors.toList());
        if (parser.size() != listenerDefinition.getParsers().size()) {
            throw new IllegalArgumentException("Each parser must be of type TcpParser but was not.");
        }
        final TcpListener listener = new TcpListener(listenerDefinition.getName(), parser.iterator().next());
        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(listener);
        wrapper.setPropertyValues(listenerDefinition.getParameterMap());
        return listener;
    }
}
