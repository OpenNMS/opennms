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

package org.opennms.features.kafka.producer;

import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;

public class ProtobufMapper {

    private final EventConfDao eventConfDao;

    public ProtobufMapper(EventConfDao eventConfDao) {
        this.eventConfDao = Objects.requireNonNull(eventConfDao);
    }

    public OpennmsModelProtos.Node.Builder toNode(OnmsNode node) {
        if (node == null) {
            return null;
        }
        final OpennmsModelProtos.Node.Builder builder = OpennmsModelProtos.Node.newBuilder();
        return builder;
    }

    public OpennmsModelProtos.Event.Builder toEvent(Event event) {
        if (event == null) {
            return null;
        }
        final OpennmsModelProtos.Event.Builder builder = OpennmsModelProtos.Event.newBuilder()
                .setId(event.getDbid())
                .setUei(event.getUei())
                .setSoure(event.getSource())
                .setSeverity(toSeverity(OnmsSeverity.get(event.getSeverity())))
                .setLabel(eventConfDao.getEventLabel(event.getUei()))
                .setDescription(event.getDescr());

        if (event.getLogmsg() != null) {
            builder.setLogMessage(event.getLogmsg().getContent());
        }

        if (event.getNodeid() != null) {
            builder.setNodeCriteria(OpennmsModelProtos.NodeCriteria.newBuilder()
                    .setId(event.getNodeid())); // TODO: Lookup FS:FID
        }

        setTimeIfNotNull(event.getTime(), builder::setTime);

        return builder;
    }

    public OpennmsModelProtos.Event.Builder toEvent(OnmsEvent event) {
        if (event == null) {
            return null;
        }
        final OpennmsModelProtos.Event.Builder builder = OpennmsModelProtos.Event.newBuilder()
                .setId(event.getId())
                .setUei(event.getEventUei())
                .setSoure(event.getEventSource())
                .setSeverity(toSeverity(OnmsSeverity.get(event.getEventSeverity())))
                .setLabel(eventConfDao.getEventLabel(event.getEventUei()))
                .setDescription(event.getEventDescr())
                .setLogMessage(event.getEventLogMsg());

        if (event.getNodeId() != null) {
            builder.setNodeCriteria(toNodeCriteria(event.getNode()));
        }

        setTimeIfNotNull(event.getEventTime(), builder::setTime);

        return builder;
    }

    public OpennmsModelProtos.Alarm.Builder toAlarm(OnmsAlarm alarm) {
        final OpennmsModelProtos.Alarm.Builder builder = OpennmsModelProtos.Alarm.newBuilder()
                .setId(alarm.getId())
                .setUei(alarm.getUei())
                .setIpAddress(InetAddressUtils.toIpAddrString(alarm.getIpAddr()))
                .setReductionKey(alarm.getReductionKey())
                .setIfIndex(alarm.getIfIndex())
                .setCount(alarm.getCounter())
                .setDescription(alarm.getDescription())
                .setLogMessage(alarm.getLogMsg())
                .setOperatorInstructions(alarm.getOperInstruct())
                .setAckUser(alarm.getAckUser())
                .setClearKey(alarm.getClearKey())
                .setLastEvent(toEvent(alarm.getLastEvent()))
                .setSeverity(toSeverity(alarm.getSeverity()));

        if (alarm.getNodeId() != null) {
            builder.setNodeCriteria(OpennmsModelProtos.NodeCriteria.newBuilder()
                    .setId(alarm.getNodeId())
                    .setForeignSource(alarm.getNode().getForeignSource())
                    .setForeignId(alarm.getNode().getForeignId()));
        }

        OpennmsModelProtos.Alarm.Type type = OpennmsModelProtos.Alarm.Type.UNRECOGNIZED;
        if (alarm.getAlarmType() != null) {
            if (alarm.getAlarmType() == OnmsAlarm.PROBLEM_TYPE) {
                type = OpennmsModelProtos.Alarm.Type.PROBLEM_WITH_CLEAR;
            } else if (alarm.getAlarmType() == OnmsAlarm.RESOLUTION_TYPE) {
                type = OpennmsModelProtos.Alarm.Type.CLEAR;
            } else if (alarm.getAlarmType() == OnmsAlarm.PROBLEM_WITHOUT_RESOLUTION_TYPE) {
                type = OpennmsModelProtos.Alarm.Type.PROBLEM_WITHOUT_CLEAR;
            }
        }
        builder.setType(type);

        if (alarm.getServiceType() != null) {
            builder.setServiceName(alarm.getServiceType().getName());
        }

        setTimeIfNotNull(alarm.getFirstEventTime(), builder::setFirstEventTime);
        setTimeIfNotNull(alarm.getLastEventTime(), builder::setLastEventTime);
        setTimeIfNotNull(alarm.getAckTime(), builder::setAckTime);

        return builder;
    }

    public OpennmsModelProtos.NodeCriteria.Builder toNodeCriteria(OnmsNode node) {
        return OpennmsModelProtos.NodeCriteria.newBuilder()
                .setId(node.getId())
                .setForeignSource(node.getForeignSource())
                .setForeignId(node.getForeignId());
    }

    public OpennmsModelProtos.Severity toSeverity(OnmsSeverity sev) {
        final OpennmsModelProtos.Severity severity;
        switch(sev) {
            case INDETERMINATE:
                severity = OpennmsModelProtos.Severity.INDETERMINATE;
                break;
            case CLEARED:
                severity = OpennmsModelProtos.Severity.CLEARED;
                break;
            case NORMAL:
                severity = OpennmsModelProtos.Severity.NORMAL;
                break;
            case WARNING:
                severity = OpennmsModelProtos.Severity.WARNING;
                break;
            case MINOR:
                severity = OpennmsModelProtos.Severity.MINOR;
                break;
            case MAJOR:
                severity = OpennmsModelProtos.Severity.MAJOR;
                break;
            case CRITICAL:
                severity = OpennmsModelProtos.Severity.CRITICAL;
                break;
            default:
                severity = OpennmsModelProtos.Severity.UNRECOGNIZED;
        }
        return severity;
    }

    private static void setTimeIfNotNull(Date date, Consumer<Long> setter) {
        if (date != null) {
            setter.accept(date.getTime());
        }
    }
}
