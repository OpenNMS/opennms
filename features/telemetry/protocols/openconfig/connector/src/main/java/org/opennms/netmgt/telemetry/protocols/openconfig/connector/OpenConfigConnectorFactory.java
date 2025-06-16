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
package org.opennms.netmgt.telemetry.protocols.openconfig.connector;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.features.openconfig.api.OpenConfigClientFactory;
import org.opennms.netmgt.telemetry.api.receiver.Connector;
import org.opennms.netmgt.telemetry.api.receiver.ConnectorFactory;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.api.registry.TelemetryRegistry;
import org.opennms.netmgt.telemetry.config.api.ConnectorDefinition;

public class OpenConfigConnectorFactory implements ConnectorFactory {

    private final TelemetryRegistry telemetryRegistry;

    private final OpenConfigClientFactory clientFactory;

    public OpenConfigConnectorFactory(TelemetryRegistry telemetryRegistry, OpenConfigClientFactory clientFactory) {
        this.telemetryRegistry = telemetryRegistry;
        this.clientFactory = clientFactory;
    }

    @Override
    public Class<? extends Connector> getBeanClass() {
        return OpenConfigConnector.class;
    }

    @Override
    public Connector createBean(ConnectorDefinition connectorDefinition) {
        final AsyncDispatcher<TelemetryMessage> dispatcher = telemetryRegistry.getDispatcher(connectorDefinition.getQueueName());
        return new OpenConfigConnector(dispatcher, clientFactory);
    }
}
