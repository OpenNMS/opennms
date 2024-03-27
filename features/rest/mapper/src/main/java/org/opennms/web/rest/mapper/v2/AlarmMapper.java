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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.web.rest.model.v2.AlarmDTO;
import org.opennms.web.rest.model.v2.AlarmSummaryDTO;
import org.opennms.web.rest.model.v2.EventParameterDTO;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public abstract class AlarmMapper {

    private String ticketUrlTemplate = System.getProperty("opennms.alarmTroubleTicketLinkTemplate");

    @Autowired
    private EventConfDao eventConfDao;

    @Mappings({
            @Mapping(source = "distPoller.location", target = "location"),
            @Mapping(source = "ipAddr", target = "ipAddress"),
            @Mapping(source = "alarmType", target = "type"),
            @Mapping(ignore = true, target = "relatedAlarms"),
            @Mapping(source = "counter", target = "count"),
            @Mapping(source = "severityLabel", target = "severity"),
            @Mapping(source = "logMsg", target = "logMessage"),
            @Mapping(source = "operInstruct", target = "operatorInstructions"),
            @Mapping(source = "TTicketId", target = "troubleTicket"),
            @Mapping(source = "TTicketState", target = "troubleTicketState"),
            @Mapping(source = "alarmAckUser", target = "ackUser"),
            @Mapping(source = "alarmAckTime", target = "ackTime"),
            @Mapping(source = "suppressedUser", target = "suppressedBy")
    })
    public abstract AlarmDTO alarmToAlarmDTO(OnmsAlarm alarm);

    @InheritInverseConfiguration
    public abstract OnmsAlarm alarmDTOToAlarm(AlarmDTO alarm);

    public Integer ackTypeToInteger(AckType ack) {
        return ack.getId();
    }

    @AfterMapping
    protected void fillAlarm(OnmsAlarm alarm, @MappingTarget AlarmDTO alarmDTO) {
        final List<OnmsEventParameter> eventParms = alarm.getEventParameters();
        if (eventParms != null) {
            alarmDTO.setParameters(eventParms.stream()
                    .map(this::eventParameterToEventParameterDTO)
                    .collect(Collectors.toList()));
        }
        if (alarm.getTTicketId() != null && !alarm.getTTicketId().isEmpty() && ticketUrlTemplate != null) {
            alarmDTO.setTroubleTicketLink(getTicketUrl(alarm.getTTicketId()));
        }
        // If there are no related alarms, we do not add them to the DTO and
        // the field will not be serialized.
        if (alarm.isSituation()) {
            alarmDTO.setRelatedAlarms(alarm.getRelatedAlarms().stream()
                                      .map(this::alarmToAlarmSummaryDTO)
                                      .sorted(Comparator.comparing(AlarmSummaryDTO::getId))
                                      .collect(Collectors.toList()));
        }
    }

    protected Integer mapTicketStateToInt(TroubleTicketState state) {
        if (state == null) {
            return null;
        }
        return state.getValue();
    }

    protected TroubleTicketState mapIntToTicketState(Integer value) {
        return Arrays.stream(TroubleTicketState.values())
                .filter(s -> Objects.equals(value, s.getValue()))
                .findFirst()
                .orElse(null);
    }

    public abstract EventParameterDTO eventParameterToEventParameterDTO(OnmsEventParameter eventParameter);

    @Mappings({
        @Mapping(source = "id", target = "id"),
        @Mapping(source = "type", target = "type"),
        @Mapping(source = "severity", target = "severity"),
        @Mapping(source = "reductionKey", target = "reductionKey"),
        @Mapping(source = "description", target = "description"),
        @Mapping(source = "lastEvent.eventUei", target = "uei"),
        @Mapping(source = "nodeLabel", target = "nodeLabel"),
        @Mapping(source = "logMsg", target = "logMessage"),
    })
    public abstract AlarmSummaryDTO alarmToAlarmSummaryDTO(OnmsAlarm alarm);
    
    public void setTicketUrlTemplate(String ticketUrlTemplate) {
        this.ticketUrlTemplate = ticketUrlTemplate;
    }

    // DO NOT MAKE protected or public as it then will be used for ALL String mappings
    private String getTicketUrl(String ticketId) {
        Objects.requireNonNull(ticketUrlTemplate);
        Objects.requireNonNull(ticketId);
        return ticketUrlTemplate.replaceAll("\\$\\{id\\}", ticketId);
    }

    @AfterMapping
    protected void mapEventLabel(@MappingTarget AlarmSummaryDTO summaryDTO) {
        //there are cases when lasteventid in alarms is null, making api/v2/alarms throw a null pointer exception
        if(summaryDTO.getUei() != null) {
            summaryDTO.setLabel(eventConfDao.getEventLabel(summaryDTO.getUei()));
        }
    }

    public void setEventConfDao(EventConfDao eventConfDao) {
        this.eventConfDao = eventConfDao;
    }
}
