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

import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.ListenerFactory;
import org.opennms.netmgt.telemetry.config.api.ListenerDefinition;
import org.opennms.netmgt.telemetry.listeners.TcpListener;
import org.opennms.netmgt.telemetry.listeners.TcpParser;

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
        return new TcpListener(listenerDefinition.getName(), parser.iterator().next(), telemetryRegistry.getMetricRegistry());
    }
}
