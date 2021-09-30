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
