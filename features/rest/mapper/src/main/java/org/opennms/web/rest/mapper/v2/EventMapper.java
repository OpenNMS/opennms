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

import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.web.rest.model.v2.EventDTO;
import org.opennms.web.rest.model.v2.EventParameterDTO;
import org.opennms.web.rest.model.v2.ServiceTypeDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {})
public abstract class EventMapper {

    @Autowired
    private EventConfDao eventConfDao;

    @Mappings({
            @Mapping(source = "eventUei", target = "uei"),
            @Mapping(source = "eventTime", target = "time"),
            @Mapping(source = "eventHost", target = "host"),
            @Mapping(source = "eventSource", target = "source"),
            @Mapping(source = "ipAddr", target = "ipAddress"),
            @Mapping(source = "eventSnmpHost", target = "snmpHost"),
            @Mapping(source = "eventSnmp", target = "snmp"),
            @Mapping(source = "eventCreateTime", target = "createTime"),
            @Mapping(source = "eventDescr", target = "description"),
            @Mapping(source = "eventLogGroup", target = "logGroup"),
            @Mapping(source = "eventLogMsg", target = "logMessage"),
            @Mapping(source = "eventPathOutage", target = "pathOutage"),
            @Mapping(source = "eventCorrelation", target = "correlation"),
            @Mapping(source = "eventSuppressedCount", target = "suppressedCount"),
            @Mapping(source = "eventOperInstruct", target = "operatorInstructions"),
            @Mapping(source = "eventAutoAction", target = "autoAction"),
            @Mapping(source = "eventOperAction", target = "operatorAction"),
            @Mapping(source = "eventOperActionMenuText", target = "operationActionMenuText"),
            @Mapping(source = "eventNotification", target = "notification"),
            @Mapping(source = "eventTTicket", target = "troubleTicket"),
            @Mapping(source = "eventTTicketState", target = "troubleTicketState"),
            @Mapping(source = "eventMouseOverText", target = "mouseOverText"),
            @Mapping(source = "eventLog", target = "log"),
            @Mapping(source = "eventDisplay", target = "display"),
            @Mapping(source = "eventAckUser", target = "ackUser"),
            @Mapping(source = "eventAckTime", target = "ackTime"),
            @Mapping(source = "distPoller.location", target = "location"),
            @Mapping(source = "severityLabel", target = "severity")
    })
    public abstract EventDTO eventToEventDTO(OnmsEvent event);

    @InheritInverseConfiguration
    public abstract OnmsEvent eventDTOToEvent(EventDTO event);

    public abstract ServiceTypeDTO serviceTypeToServiceTypeDTO(OnmsServiceType serviceType);

    public abstract EventParameterDTO eventParameterToEventParameterDTO(OnmsEventParameter eventParameter);

    @AfterMapping
    protected void fillEvent(OnmsEvent event, @MappingTarget EventDTO eventDTO) {
        final List<OnmsEventParameter> eventParms = event.getEventParameters();
        if (eventParms != null) {
            eventDTO.setParameters(eventParms.stream()
                    .map(this::eventParameterToEventParameterDTO)
                    .collect(Collectors.toList()));
        }
        eventDTO.setSeverity(event.getSeverityLabel());
        eventDTO.setLabel(eventConfDao.getEventLabel(eventDTO.getUei()));
    }

    public void setEventConfDao(EventConfDao eventConfDao) {
        this.eventConfDao = eventConfDao;
    }
}
