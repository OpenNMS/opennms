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
package org.opennms.netmgt.telemetry.api.registry;

import java.util.Collection;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.adapter.Adapter;
import org.opennms.netmgt.telemetry.api.receiver.Connector;
import org.opennms.netmgt.telemetry.api.receiver.Listener;
import org.opennms.netmgt.telemetry.api.receiver.Parser;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.config.api.ConnectorDefinition;
import org.opennms.netmgt.telemetry.config.api.ListenerDefinition;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;

import com.codahale.metrics.MetricRegistry;

public interface TelemetryRegistry {
    Adapter getAdapter(AdapterDefinition adapterDefinition);
    Listener getListener(ListenerDefinition listenerDefinition);
    Connector getConnector(ConnectorDefinition connectorDefinition);
    Parser getParser(ParserDefinition parserDefinition);

    void registerDispatcher(String queueName, AsyncDispatcher<TelemetryMessage> dispatcher);
    void clearDispatchers();
    void removeDispatcher(String queueName);
    Collection<AsyncDispatcher<TelemetryMessage>> getDispatchers();
    AsyncDispatcher<TelemetryMessage> getDispatcher(String queueName);

    MetricRegistry getMetricRegistry();
}
