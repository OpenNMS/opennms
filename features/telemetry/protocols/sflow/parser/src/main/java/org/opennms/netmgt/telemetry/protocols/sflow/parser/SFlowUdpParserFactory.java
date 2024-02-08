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
package org.opennms.netmgt.telemetry.protocols.sflow.parser;

import java.util.Objects;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.api.receiver.ParserFactory;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class SFlowUdpParserFactory implements ParserFactory {

    private final TelemetryRegistry telemetryRegistry;
    private final DnsResolver dnsResolver;

    public SFlowUdpParserFactory(TelemetryRegistry telemetryRegistry, DnsResolver dnsResolver) {
        this.telemetryRegistry = Objects.requireNonNull(telemetryRegistry);
        this.dnsResolver = Objects.requireNonNull(dnsResolver);
    }

    @Override
    public Class<? extends Parser> getBeanClass() {
        return SFlowUdpParser.class;
    }

    @Override
    public Parser createBean(ParserDefinition beanDefinition) {
        final AsyncDispatcher<TelemetryMessage> dispatcher = telemetryRegistry.getDispatcher(beanDefinition.getQueueName());
        return new SFlowUdpParser(beanDefinition.getFullName(), dispatcher, dnsResolver);
    }
}
