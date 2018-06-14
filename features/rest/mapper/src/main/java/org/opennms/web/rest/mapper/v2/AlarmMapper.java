/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.mapper.v2;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.Situation;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.web.rest.model.v2.AlarmDTO;
import org.opennms.web.rest.model.v2.AlarmSummaryDTO;
import org.opennms.web.rest.model.v2.EventParameterDTO;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public abstract class AlarmMapper {

    private String ticketUrlTemplate = System.getProperty("opennms.alarmTroubleTicketLinkTemplate");

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
        if (alarm instanceof Situation) {
            alarmDTO.setRelatedAlarms(((Situation)alarm).getAlarms().stream().map(a -> alarmToAlarmSummaryDTO(a)).collect(Collectors.toList()));
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
}
