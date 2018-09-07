/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import java.nio.ByteBuffer;
import java.util.Map;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.simple.SimpleUdpParser;
import org.opennms.netmgt.telemetry.listeners.smart.SmartUdpParser;
import org.opennms.netmgt.telemetry.protocols.common.parser.ForwardParser;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.proto.NetflowPacket;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.Netflow9UdpParser;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

public class Netflow5 implements SmartUdpParser.Factory {
    // TODO (fooker): This should do parsing on the receiver side for protocol separation

    public static class Parser extends ForwardParser.Parser implements SmartUdpParser, SimpleUdpParser {

        public Parser(final String name,
                      final AsyncDispatcher<TelemetryMessage> dispatcher) {
            super(name, dispatcher);
        }

        @Override
        public boolean handles(final ByteBuffer buffer) {
            return BufferUtils.uint16(buffer) == NetflowPacket.VERSION;
        }
    }

    @Override
    public SmartUdpParser createUdpParser(final String name,
                                          final Map<String, String> parameters,
                                          final AsyncDispatcher<TelemetryMessage> dispatcher) {
        return new Parser(name, dispatcher);
    }

}
