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

package org.opennms.netmgt.telemetry.adapters.netflow;

import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.NetflowPacket;
import org.opennms.netmgt.telemetry.config.model.Protocol;
import org.opennms.netmgt.telemetry.ipc.TelemetryMessageDTO;
import org.opennms.netmgt.telemetry.ipc.TelemetryMessageLogDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Netflow5Adapter implements Adapter {


    private static final Logger LOG = LoggerFactory.getLogger(Netflow5Adapter.class);

    @Override
    public void setProtocol(Protocol protocol) {
        // we do not need the protocol
    }

    @Override
    public void handleMessageLog(TelemetryMessageLogDTO messageLog) {
        LOG.debug("Received {} telemetry messages", messageLog.getMessages().size());

        for (TelemetryMessageDTO eachMessage : messageLog.getMessages()) {
            LOG.debug("Parse log message {}", eachMessage);

            // Create NetflowPacket which delegates all calls to the byte array
            try {
                final NetflowPacket flowPacket = new NetflowPacket(eachMessage.getBytes());
                if (flowPacket.getVersion() != NetflowPacket.VERSION) {
                    LOG.warn("Invalid Version. Expected {}, received {}. Skipping flow packet.", NetflowPacket.VERSION, flowPacket.getVersion());
                    continue;
                }
                if (flowPacket.getCount() == 0) {
                    LOG.warn("Received packet has no content. Skipping flow packet.");
                    continue;
                }
                // TODO MVR an invalid packet is skipped for now, but we may want to persist it anyways
                if (!flowPacket.isValid()) {
                    LOG.warn("Received packet is not valid. Skipping flow packet.");
                    continue;
                }
                LOG.debug("Flow packet received: {}", flowPacket);

            } catch (Exception e) {
                LOG.error("Received packet cannot be read.", e);
            }
        }
    }
}
