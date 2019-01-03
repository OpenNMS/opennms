/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer.mapping;

import java.util.concurrent.ExecutionException;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used by MapStruct to generate mapping code.
 */
@Mapper(uses = {ProtoBuilderFactory.class, NodeCriteriaMapper.class, SeverityMapper.class, DateMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface EventMapper {
    Logger LOG = LoggerFactory.getLogger(EventMapper.class);

    @Mapping(source = "dbid", target = "id")
    @Mapping(source = "descr", target = "description")
    @Mapping(source = "logmsg", target = "logMessage")
    @Mapping(source = "interface", target = "ipAddress")
    OpennmsModelProtos.Event.Builder map(Event event, @Context MappingContext mappingContext);

    @AfterMapping
    default void afterMapping(Event event, @MappingTarget OpennmsModelProtos.Event.Builder eventBuilder,
                              @Context MappingContext mappingContext) {
        eventBuilder.setLabel(mappingContext.getEventConfDao().getEventLabel(event.getUei()));

        if (event.getNodeid() != null) {
            try {
                eventBuilder.setNodeCriteria(mappingContext.getNodeIdToCriteriaCache().get(event.getNodeid()));
            } catch (ExecutionException e) {
                LOG.warn("An error occurred when building node criteria for node with id: {}." +
                                " The node foreign source and foreign id (if set) will be missing from the event with" +
                                " id: {}.",
                        event.getNodeid(), event.getDbid(), e);
                eventBuilder.setNodeCriteria(OpennmsModelProtos.NodeCriteria.newBuilder()
                        .setId(event.getNodeid()));
            }
            // We only include the node id in the node criteria in when forwarding events
            // since the event does not currently contain the fs:fid or a reference to the node object.
            eventBuilder.setNodeCriteria(OpennmsModelProtos.NodeCriteria.newBuilder()
                    .setId(event.getNodeid()));
        }
    }

    default String mapLogMsgToString(Logmsg logMsg) {
        // protobuf does not allow null strings, instead it defaults to ""
        return logMsg.getContent() == null ? "" : logMsg.getContent();
    }
}
