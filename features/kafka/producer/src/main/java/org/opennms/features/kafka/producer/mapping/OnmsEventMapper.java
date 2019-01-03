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

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;

/**
 * Used by MapStruct to generate mapping code.
 */
@Mapper(uses = {ProtoBuilderFactory.class, NodeCriteriaMapper.class, SeverityMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OnmsEventMapper {
    @Mapping(source = "eventUei", target = "uei")
    @Mapping(source = "eventSource", target = "source")
    @Mapping(source = "eventSeverity", target = "severity")
    @Mapping(source = "eventLog", target = "log")
    @Mapping(source = "eventDisplay", target = "display")
    @Mapping(source = "eventDescr", target = "description")
    @Mapping(source = "eventLogMsg", target = "logMessage")
    @Mapping(source = "node", target = "nodeCriteria")
    OpennmsModelProtos.Event.Builder map(OnmsEvent event, @Context MappingContext mappingContext);

    @AfterMapping
    default void afterMapping(OnmsEvent event, @MappingTarget OpennmsModelProtos.Event.Builder eventBuilder,
                              @Context MappingContext mappingContext) {
        final String eventLabel = mappingContext.getEventConfDao().getEventLabel(event.getEventUei());
        if (eventLabel != null) {
            eventBuilder.setLabel(eventLabel);
        }

        for (OnmsEventParameter param : event.getEventParameters()) {
            if (param.getName() == null || param.getValue() == null) {
                continue;
            }
            eventBuilder.addParameter(OpennmsModelProtos.EventParameter.newBuilder()
                    .setName(param.getName())
                    .setValue(param.getValue()));
        }

        mappingContext.setTimeIfNotNull(event.getEventTime(), eventBuilder::setTime);
        mappingContext.setTimeIfNotNull(event.getEventCreateTime(), eventBuilder::setTime);
    }

    default boolean mapEventStringToBoolean(String eventString) {
        return "Y".equalsIgnoreCase(eventString);
    }
    
    default OpennmsModelProtos.Event buildEvent(OnmsEvent event, @Context MappingContext mappingContext) {
        return event == null ? null : map(event, mappingContext).build();
    }
}
