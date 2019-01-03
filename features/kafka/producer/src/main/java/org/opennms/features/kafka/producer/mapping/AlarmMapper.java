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
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsServiceType;

/**
 * Used by MapStruct to generate mapping code.
 */
@Mapper(uses = {ProtoBuilderFactory.class, SeverityMapper.class, InetAddressMapper.class, NodeCriteriaMapper.class,
        AlarmTypeMapper.class, DateMapper.class, OnmsEventMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AlarmMapper {
    @Mapping(source = "counter", target = "count")
    @Mapping(source = "logMsg", target = "logMessage")
    @Mapping(source = "ipAddr", target = "ipAddress")
    @Mapping(source = "operInstruct", target = "operatorInstructions")
    @Mapping(source = "node", target = "nodeCriteria")
    @Mapping(source = "alarmType", target = "type")
    @Mapping(source = "serviceType", target = "serviceName")
    OpennmsModelProtos.Alarm.Builder map(OnmsAlarm alarm, @Context MappingContext mappingContext);

    @AfterMapping
    default void afterMapping(OnmsAlarm alarm, @MappingTarget OpennmsModelProtos.Alarm.Builder alarmBuilder,
                              @Context MappingContext mappingContext) {
        if (alarm.getRelatedAlarms() != null) {
            alarm.getRelatedAlarms().forEach(relatedAlarm -> alarmBuilder.addRelatedAlarm(map(relatedAlarm,
                    mappingContext)));
        }
    }

    default String mapServiceNameToServiceType(OnmsServiceType serviceType) {
        // protobuf does not allow null strings, instead it defaults to ""
        return serviceType.getName() == null ? "" : serviceType.getName();
    }
}
