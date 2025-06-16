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
package org.opennms.netmgt.telemetry.listeners.factory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.ListenerFactory;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.api.ListenerDefinition;
import org.opennms.netmgt.telemetry.listeners.UdpListener;
import org.opennms.netmgt.telemetry.listeners.UdpParser;

public class UdpListenerFactory implements ListenerFactory {

    private TelemetryRegistry telemetryRegistry;

    public UdpListenerFactory(TelemetryRegistry telemetryRegistry) {
        this.telemetryRegistry = Objects.requireNonNull(telemetryRegistry);
    }

    @Override
    public Class<? extends Listener> getBeanClass() {
        return UdpListener.class;
    }

    @Override
    public Listener createBean(ListenerDefinition listenerDefinition) {
        // Ensure each defined parser is of type UdpParser
        final List<Parser> parsers = listenerDefinition.getParsers().stream()
                .map(p -> telemetryRegistry.getParser(p))
                .collect(Collectors.toList());
        final List<UdpParser> udpParsers = parsers.stream()
                .filter(p -> p instanceof UdpParser)
                .map(p -> (UdpParser) p).collect(Collectors.toList());
        if (parsers.size() != udpParsers.size()) {
            throw new IllegalArgumentException("Each parser must be of type UdpParser but was not: " + parsers);
        }
        return new UdpListener(listenerDefinition.getName(), udpParsers, telemetryRegistry.getMetricRegistry());
    }
}
