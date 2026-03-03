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
package org.opennms.web.rest.mapper.v2;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.events.store.StoredEvent;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.model.v2.EventDTO;
import org.opennms.web.rest.model.v2.EventParameterDTO;

/**
 * Maps {@link StoredEvent} (from events_archive) to {@link EventDTO} (REST response).
 */
public class StoredEventMapper {

    private final EventConfDao eventConfDao;

    public StoredEventMapper(EventConfDao eventConfDao) {
        this.eventConfDao = eventConfDao;
    }

    public EventDTO toEventDTO(StoredEvent event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getEventTsid());
        dto.setUei(event.getEventUei());
        dto.setSource(event.getEventSource());
        dto.setSeverity(severityLabel(event.getEventSeverity()));
        dto.setTime(Date.from(event.getEventTime()));
        dto.setCreateTime(event.getCreatedAt() != null ? Date.from(event.getCreatedAt()) : null);
        dto.setNodeId(event.getNodeId() != null ? event.getNodeId().intValue() : null);
        dto.setDescription(event.getEventDescr());
        dto.setLogMessage(event.getEventLogMsg());
        dto.setLog(event.getEventLog());
        dto.setDisplay(event.getEventDisplay());

        if (event.getIpAddress() != null) {
            InetAddress addr = InetAddressUtils.addr(event.getIpAddress());
            dto.setIpAddress(addr);
        }

        if (event.getEventData() != null && !event.getEventData().isEmpty()) {
            dto.setParameters(toParameterDTOs(event.getEventData()));
        }

        if (eventConfDao != null) {
            dto.setLabel(eventConfDao.getEventLabel(event.getEventUei()));
        }

        return dto;
    }

    private static List<EventParameterDTO> toParameterDTOs(Map<String, String> eventData) {
        List<EventParameterDTO> params = new ArrayList<>(eventData.size());
        for (Map.Entry<String, String> entry : eventData.entrySet()) {
            EventParameterDTO param = new EventParameterDTO();
            param.setName(entry.getKey());
            param.setValue(entry.getValue());
            param.setType("string");
            params.add(param);
        }
        return params;
    }

    private static String severityLabel(int severity) {
        OnmsSeverity s = OnmsSeverity.get(severity);
        return s != null ? s.getLabel() : "Indeterminate";
    }
}
