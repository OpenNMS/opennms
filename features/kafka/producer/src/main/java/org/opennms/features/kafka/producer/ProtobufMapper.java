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
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
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

        final OpennmsModelProtos.Node.Builder builder = OpennmsModelProtos.Node.newBuilder()
                .setId(node.getId())
                .setLabel(node.getLabel())
                .setLocation(node.getLocation().getLocationName());
        if (node.getForeignSource() != null) {
            builder.setForeignSource(node.getForeignSource());
        }
        if (node.getForeignId() != null) {
            builder.setForeignId(node.getForeignId());
        }
        if (node.getSysContact() != null) {
            builder.setSysContact(node.getSysContact());
        }
        if (node.getSysDescription() != null) {
            builder.setSysDescription(node.getSysDescription());
        }
        if (node.getSysObjectId() != null) {
            builder.setSysObjectId(node.getSysObjectId());
        }

        // Add all of the intefaces
        node.getSnmpInterfaces().forEach(s -> builder.addSnmpInterface(toSnmpInterface(s)));
        node.getIpInterfaces().forEach(i -> builder.addIpInterface(toIpInterface(i)));

        // Add all of the categories, sorting them by name first
        node.getCategories()
                .stream()
                .map(OnmsCategory::getName)
                .sorted()
                .forEach(builder::addCategory);

        setTimeIfNotNull(node.getCreateTime(), builder::setCreateTime);

        return builder;
    }

    public OpennmsModelProtos.Event.Builder toEvent(Event event) {
        if (event == null) {
            return null;
        }
        final OpennmsModelProtos.Event.Builder builder = OpennmsModelProtos.Event.newBuilder()
                .setId(event.getDbid())
                .setUei(event.getUei())
                .setSource(event.getSource())
                .setSeverity(toSeverity(OnmsSeverity.get(event.getSeverity())))
                .setLabel(eventConfDao.getEventLabel(event.getUei()));
        if (event.getDescr() != null) {
            builder.setDescription(event.getDescr());
        }

        if (event.getLogmsg() != null) {
            builder.setLogMessage(event.getLogmsg().getContent());
        }
        if (event.getNodeid() != null) {
            // We only include the node id in the node criteria in when forwarding events
            // since the event does not currently contain the fs:fid or a reference to the node object.
            builder.setNodeCriteria(OpennmsModelProtos.NodeCriteria.newBuilder()
                    .setId(event.getNodeid()));
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
                .setSource(event.getEventSource())
                .setSeverity(toSeverity(OnmsSeverity.get(event.getEventSeverity())))
                .setLabel(eventConfDao.getEventLabel(event.getEventUei()))
                .setLog("Y".equalsIgnoreCase(event.getEventLog()))
                .setDisplay("Y".equalsIgnoreCase(event.getEventDisplay()));

        if (event.getEventDescr() != null) {
            builder.setDescription(event.getEventDescr());
        }
        if (event.getEventLogMsg() != null) {
            builder.setLogMessage(event.getEventLogMsg());
        }
        if (event.getNodeId() != null) {
            builder.setNodeCriteria(toNodeCriteria(event.getNode()));
        }

        for (OnmsEventParameter param : event.getEventParameters()) {
            if (param.getName() == null || param.getValue() == null) {
                continue;
            }
            builder.addParameter(OpennmsModelProtos.EventParameter.newBuilder()
                    .setName(param.getName())
                    .setValue(param.getValue()));
        }

        setTimeIfNotNull(event.getEventTime(), builder::setTime);
        setTimeIfNotNull(event.getEventCreateTime(), builder::setTime);

        return builder;
    }

    public OpennmsModelProtos.Alarm.Builder toAlarm(OnmsAlarm alarm) {
        final OpennmsModelProtos.Alarm.Builder builder = OpennmsModelProtos.Alarm.newBuilder()
                .setId(alarm.getId())
                .setUei(alarm.getUei())
                .setCount(alarm.getCounter())
                .setSeverity(toSeverity(alarm.getSeverity()));

        if (alarm.getReductionKey() != null) {
            builder.setReductionKey(alarm.getReductionKey());
        }
        if (toEvent(alarm.getLastEvent()) != null) {
            builder.setLastEvent(toEvent(alarm.getLastEvent()));
        }
        if (alarm.getLogMsg() != null) {
            builder.setLogMessage(alarm.getLogMsg());
        }
        if (alarm.getDescription() != null) {
            builder.setDescription(alarm.getDescription());
        }
        if (alarm.getIpAddr() != null) {
            builder.setIpAddress(InetAddressUtils.toIpAddrString(alarm.getIpAddr()));
        }
        if (alarm.getIfIndex() != null) {
            builder.setIfIndex(alarm.getIfIndex());
        }
        if (alarm.getOperInstruct() != null) {
            builder.setOperatorInstructions(alarm.getOperInstruct());
        }
        if (alarm.getAckUser() != null) {
            builder.setAckUser(alarm.getAckUser());
        }
        if (alarm.getClearKey() != null) {
            builder.setClearKey(alarm.getClearKey());
        }
        if (alarm.getNodeId() != null) {
            builder.setNodeCriteria(toNodeCriteria(alarm.getNode()));
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
        final OpennmsModelProtos.NodeCriteria.Builder builder = OpennmsModelProtos.NodeCriteria.newBuilder()
                .setId(node.getId());
        if (node.getForeignSource() != null) {
            builder.setForeignSource(node.getForeignSource());
        }
        if (node.getForeignId() != null) {
            builder.setForeignId(node.getForeignId());
        }
        return builder;
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

    public OpennmsModelProtos.IpInterface.Builder toIpInterface(OnmsIpInterface ipInterface) {
        if (ipInterface == null) {
            return null;
        }

        final OpennmsModelProtos.IpInterface.Builder builder = OpennmsModelProtos.IpInterface.newBuilder()
                .setId(ipInterface.getId())
                .setIpAddress(InetAddressUtils.toIpAddrString(ipInterface.getIpAddress()));
        final OnmsSnmpInterface snmpInterface = ipInterface.getSnmpInterface();
        if (snmpInterface != null && snmpInterface.getIfIndex() != null) {
            builder.setIfIndex(snmpInterface.getIfIndex());
        }
        final PrimaryType primaryType = ipInterface.getIsSnmpPrimary();
        if (PrimaryType.PRIMARY.equals(primaryType)) {
            builder.setPrimaryType(OpennmsModelProtos.IpInterface.PrimaryType.PRIMARY);
        } else if (PrimaryType.SECONDARY.equals(primaryType)) {
            builder.setPrimaryType(OpennmsModelProtos.IpInterface.PrimaryType.SECONDARY);
        } else if (PrimaryType.NOT_ELIGIBLE.equals(primaryType)) {
            builder.setPrimaryType(OpennmsModelProtos.IpInterface.PrimaryType.NOT_ELIGIBLE);
        }
        ipInterface.getMonitoredServices().forEach(svc -> builder.addService(svc.getServiceName()));

        return builder;
    }

    public OpennmsModelProtos.SnmpInterface.Builder toSnmpInterface(OnmsSnmpInterface snmpInterface) {
        if (snmpInterface == null) {
            return null;
        }

        final OpennmsModelProtos.SnmpInterface.Builder builder = OpennmsModelProtos.SnmpInterface.newBuilder()
                .setId(snmpInterface.getId())
                .setIfIndex(snmpInterface.getIfIndex());
        if (snmpInterface.getIfDescr() != null) {
            builder.setIfDescr(snmpInterface.getIfDescr());
        }
        if (snmpInterface.getIfType() != null) {
            builder.setIfType(snmpInterface.getIfType());
        }
        if (snmpInterface.getIfName() != null) {
            builder.setIfName(snmpInterface.getIfName());
        }
        if (snmpInterface.getIfSpeed() != null) {
            builder.setIfSpeed(snmpInterface.getIfSpeed());
        }
        if (snmpInterface.getPhysAddr() != null) {
            builder.setIfPhysAddress(snmpInterface.getPhysAddr());
        }
        if (snmpInterface.getIfAdminStatus() != null) {
            builder.setIfAdminStatus(snmpInterface.getIfAdminStatus());
        }
        if (snmpInterface.getIfOperStatus() != null) {
            builder.setIfOperStatus(snmpInterface.getIfOperStatus());
        }
        if (snmpInterface.getIfAlias() != null) {
            builder.setIfAlias(snmpInterface.getIfAlias());
        }
        return builder;
    }

    private static void setTimeIfNotNull(Date date, Consumer<Long> setter) {
        if (date != null) {
            setter.accept(date.getTime());
        }
    }
}
