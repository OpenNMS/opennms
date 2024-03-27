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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.factory;

import java.util.Objects;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.api.receiver.ParserFactory;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Netflow9UdpParser;

public class Netflow9UdpParserFactory implements ParserFactory  {

    private final TelemetryRegistry telemetryRegistry;
    private final EventForwarder eventForwarder;
    private final Identity identity;
    private final DnsResolver dnsResolver;

    public Netflow9UdpParserFactory(final TelemetryRegistry telemetryRegistry, final EventForwarder eventForwarder, final Identity identity, final DnsResolver dnsResolver) {
        this.telemetryRegistry = Objects.requireNonNull(telemetryRegistry);
        this.eventForwarder =  Objects.requireNonNull(eventForwarder);
        this.identity = Objects.requireNonNull(identity);
        this.dnsResolver = Objects.requireNonNull(dnsResolver);
    }

    @Override
    public Class<? extends Parser> getBeanClass() {
        return Netflow9UdpParser.class;
    }

    @Override
    public Parser createBean(ParserDefinition parserDefinition) {
        final AsyncDispatcher<TelemetryMessage> dispatcher = telemetryRegistry.getDispatcher(parserDefinition.getQueueName());
        return new Netflow9UdpParser(parserDefinition.getFullName(), dispatcher, eventForwarder, identity, dnsResolver, telemetryRegistry.getMetricRegistry());
    }
}
