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

package org.opennms.netmgt.telemetry.adapters.netflow.v5;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.api.Converter;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.proto.NetflowPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Netflow5Converter implements Converter<NetflowPacket> {

    private static final Logger LOG = LoggerFactory.getLogger(Netflow5Converter.class);

    @Override
    public List<Flow> convert(final NetflowPacket packet) {
        if (packet == null) {
            LOG.debug("Nothing to convert.");
            return Collections.emptyList();
        }
        return packet.getRecords().stream()
                .map(record -> new Netflow5Flow(packet, record))
                .collect(Collectors.toList());
    }
}
