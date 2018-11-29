/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5;

import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.protocols.flows.AbstractFlowAdapter;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.proto.NetflowPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

public class Netflow5Adapter extends AbstractFlowAdapter<NetflowPacket> {

    private static final Logger LOG = LoggerFactory.getLogger(Netflow5Adapter.class);

    public Netflow5Adapter(final MetricRegistry metricRegistry,
                           final FlowRepository flowRepository) {
        super(metricRegistry, flowRepository, new Netflow5Converter());
    }

    @Override
    protected NetflowPacket parse(TelemetryMessageLogEntry message) {
        // Create NetflowPacket which delegates all calls to the byte array
        final NetflowPacket flowPacket = new NetflowPacket(message.getByteArray());

        // Version must match for now. Otherwise we drop the packet
        if (flowPacket.getVersion() != NetflowPacket.VERSION) {
            LOG.warn("Invalid Version. Expected {}, received {}. Dropping flow packet.", NetflowPacket.VERSION, flowPacket.getVersion());
            return null;
        }

        // Empty flows are dropped for now
        if (flowPacket.getCount() == 0) {
            LOG.warn("Received packet has no content. Dropping flow packet.");
            return null;
        }

        // Validates the parsed packet and drops it when not valid
        if (!flowPacket.isValid()) {
            LOG.warn("Received packet is not valid. Dropping flow packet.");
            return null;
        }

        return flowPacket;
    }
}
