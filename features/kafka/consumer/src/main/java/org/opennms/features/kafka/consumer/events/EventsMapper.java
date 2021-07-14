/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.consumer.events;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class EventsMapper {

    private static final Logger LOG = LoggerFactory.getLogger(EventsMapper.class);

    public static Event toEvent(EventsProto.Event pbEvent) {
        if (Strings.isNullOrEmpty(pbEvent.getUei())) {
            LOG.warn("Event will not be forwarded, `uei` is required field, skipped Event : \n {}", pbEvent);
            return null;
        }
        if (Strings.isNullOrEmpty(pbEvent.getSource())) {
            LOG.warn("Event will not be forwarded, `source` is required field, skipped Event : \n {}", pbEvent);
            return null;
        }
        final EventBuilder builder = new EventBuilder(pbEvent.getUei(), pbEvent.getSource());
        builder.setSeverity(OnmsSeverity.get(pbEvent.getSeverity().name()).getLabel());
        getString(pbEvent.getHost()).ifPresent(builder::setHost);
        if (pbEvent.getNodeId() > 0) {
            builder.setNodeid(pbEvent.getNodeId());
        }
        getString(pbEvent.getIpAddress()).ifPresent(ip -> builder.setInterface(InetAddressUtils.getInetAddress(ip)));
        getString(pbEvent.getServiceName()).ifPresent(builder::setService);
        if (pbEvent.getIfIndex() > 0) {
            builder.setIfIndex(pbEvent.getIfIndex());
        }
        getString(pbEvent.getDistPoller()).ifPresent(builder::setDistPoller);
        getString(pbEvent.getDescription()).ifPresent(builder::setDescription);
        getString(pbEvent.getLogDest()).ifPresent(builder::setLogDest);
        getString(pbEvent.getLogContent()).ifPresent(builder::setLogMessage);
        for (EventsProto.EventParameter p : pbEvent.getParameterList()) {
            builder.setParam(p.getName(), p.getValue());
        }
        return builder.getEvent();
    }

    public static List<Event> mapProtobufToEvents(List<EventsProto.Event> pbEvents) {
        return pbEvents.stream().map(EventsMapper::toEvent).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static Optional<String> getString(String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }
}
