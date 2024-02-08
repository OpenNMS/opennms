/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
