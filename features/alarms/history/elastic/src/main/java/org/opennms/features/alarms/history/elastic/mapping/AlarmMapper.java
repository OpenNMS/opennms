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

package org.opennms.features.alarms.history.elastic.mapping;

import java.net.InetAddress;
import java.util.Optional;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.opennms.core.cache.Cache;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.alarms.history.elastic.dto.AlarmDocumentDTO;
import org.opennms.features.alarms.history.elastic.dto.NodeDocumentDTO;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.TroubleTicketState;

/**
 * Used by MapStruct to generate mapping code.
 */
@Mapper(uses = {EventMapper.class, MemoMapper.class, DateMapper.class})
public interface AlarmMapper {
    @Mapping(source = "TTicketId", target = "ticketId")
    @Mapping(source = "TTicketState", target = "ticketStateId")
    @Mapping(source = "TTicketState", target = "ticketStateName")
    @Mapping(source = "alarmType", target = "type")
    @Mapping(source = "details", target = "attributes")
    @Mapping(source = "ipAddr", target = "ipAddress")
    @Mapping(source = "logMsg", target = "logMessage")
    @Mapping(source = "operInstruct", target = "operatorInstructions")
    @Mapping(source = "reductionKeyMemo", target = "journalMemo")
    // Skip the node field since we handle it explicitly for caching purposes
    @Mapping(ignore = true, target = "node")
    AlarmDocumentDTO map(OnmsAlarm alarm, @Context MappingContext mappingContext);
    
    default String mapIpAddressToString(InetAddress ipAddress) {
        return ipAddress == null ? null : InetAddressUtils.str(ipAddress);
    }

    default String mapServiceTypeToString(OnmsServiceType serviceType) {
        return serviceType == null ? null : serviceType.getName();
    }
    
    default Integer mapTroubleTicketStateToInteger(TroubleTicketState troubleTicketState) {
        return troubleTicketState == null ? null : troubleTicketState.getValue();
    }

    default String mapTroubleTicketStateToString(TroubleTicketState troubleTicketState) {
        return troubleTicketState == null ? null : troubleTicketState.toString();
    }

    @AfterMapping
    default void afterMapping(OnmsAlarm alarm, @MappingTarget AlarmDocumentDTO alarmDocumentDTO,
                              @Context MappingContext mappingContext) {
        // Build and set the node document - cache these
        if (alarm.getNodeId() != null) {
            final Optional<NodeDocumentDTO> cachedNodeDoc =
                    mappingContext.getNodeInfoCache().getIfCached(alarm.getNodeId());
            if (cachedNodeDoc != null && cachedNodeDoc.isPresent()) {
                alarmDocumentDTO.setNode(cachedNodeDoc.get());
            } else {
                // We build the document here, rather than doing it in the call to the cache loader
                // since we have complete access to the node in this context, and don't want to overload the
                // cache key
                final NodeDocumentDTO nodeDoc = Mappers.getMapper(NodeMapper.class).map(alarm.getNode());
                mappingContext.getNodeInfoCache().put(alarm.getNodeId(), Optional.of(nodeDoc));
                alarmDocumentDTO.setNode(nodeDoc);
            }
        }

        // Set the updated time to now
        alarmDocumentDTO.setUpdateTime(mappingContext.getCurrentTime());
    }

    interface MappingContext {
        Cache<Integer, Optional<NodeDocumentDTO>> getNodeInfoCache();

        long getCurrentTime();
    }
}
