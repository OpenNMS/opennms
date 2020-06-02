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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.hibernate.ObjectNotFoundException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsHwEntityAlias;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Enums;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ProtobufMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ProtobufMapper.class);

    private final EventConfDao eventConfDao;
    private final SessionUtils sessionUtils;
    private final NodeDao nodeDao;
    private final HwEntityDao hwEntityDao;
    private final LoadingCache<Long, OpennmsModelProtos.NodeCriteria> nodeIdToCriteriaCache;

    public ProtobufMapper(EventConfDao eventConfDao, HwEntityDao hwEntityDao, SessionUtils sessionUtils,
                          NodeDao nodeDao, long nodeIdToCriteriaMaxCacheSize) {
        this.eventConfDao = Objects.requireNonNull(eventConfDao);
        this.hwEntityDao = Objects.requireNonNull(hwEntityDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.nodeDao = Objects.requireNonNull(nodeDao);

        nodeIdToCriteriaCache = CacheBuilder.newBuilder()
            .maximumSize(nodeIdToCriteriaMaxCacheSize)
            .build(new CacheLoader<Long, OpennmsModelProtos.NodeCriteria>() {
                public OpennmsModelProtos.NodeCriteria load(Long nodeId)  {
                    return sessionUtils.withReadOnlyTransaction(() -> {
                        final OnmsNode node = nodeDao.get(nodeId.intValue());
                        if (node != null && node.getForeignId() != null && node.getForeignSource() != null) {
                            return OpennmsModelProtos.NodeCriteria.newBuilder()
                                    .setId(nodeId)
                                    .setForeignId(node.getForeignId())
                                    .setForeignSource(node.getForeignSource())
                                    .build();
                        } else {
                            return OpennmsModelProtos.NodeCriteria.newBuilder()
                                    .setId(nodeId)
                                    .build();
                        }
                    });
                }
            });
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

        OnmsHwEntity rootEntity = hwEntityDao.findRootByNodeId(node.getId());
        if (rootEntity != null) {
            builder.setHwInventory(toHwEntity(rootEntity));
        }

        return builder;
    }

    public static OpennmsModelProtos.HwEntity.Builder toHwEntity(OnmsHwEntity entity) {
        if (entity == null) {
            return null;
        }

        final OpennmsModelProtos.HwEntity.Builder builder = OpennmsModelProtos.HwEntity.newBuilder();

        if (entity.getId() != null) {
            builder.setEntityId(entity.getId());
        }
        if (entity.getEntPhysicalIndex() != null) {
            builder.setEntPhysicalIndex(entity.getEntPhysicalIndex());
        }
        if (entity.getEntPhysicalClass() != null) {
            builder.setEntPhysicalClass(entity.getEntPhysicalClass());
        }
        if (entity.getEntPhysicalDescr() != null) {
            builder.setEntPhysicalDescr(entity.getEntPhysicalDescr());
        }
        if (entity.getEntPhysicalIsFRU() != null) {
            builder.setEntPhysicalIsFru(entity.getEntPhysicalIsFRU());
        }
        if (entity.getEntPhysicalName() != null) {
            builder.setEntPhysicalName(entity.getEntPhysicalName());
        }
        if (entity.getEntPhysicalVendorType() != null) {
            builder.setEntPhysicalVendorType(entity.getEntPhysicalVendorType());
        }
        // Add aliases
        entity.getEntAliases()
                .stream()
                .forEach(alias -> builder.addEntHwAlias(toHwAlias(alias)));
        // Add children
        entity.getChildren()
                .stream()
                .forEach(child -> builder.addChildren(toHwEntity(child)));

        return builder;
    }

    public static OpennmsModelProtos.HwAlias.Builder toHwAlias(OnmsHwEntityAlias alias) {
        if (alias == null) {
            return null;
        }
        final OpennmsModelProtos.HwAlias.Builder builder = OpennmsModelProtos.HwAlias.newBuilder()
                .setIndex(alias.getIndex())
                .setOid(alias.getOid());
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
                .setSeverity(toSeverity(OnmsSeverity.get(event.getSeverity())));

        getString(eventConfDao.getEventLabel(event.getUei())).ifPresent(builder::setLabel);
        getString(event.getDescr()).ifPresent(builder::setDescription);

        if (event.getLogmsg() != null) {
            builder.setLogMessage(event.getLogmsg().getContent());
        }
        if (event.getNodeid() != null) {
            try {
                builder.setNodeCriteria(nodeIdToCriteriaCache.get(event.getNodeid()));
            } catch (ExecutionException e) {
                LOG.warn("An error occurred when building node criteria for node with id: {}." +
                        " The node foreign source and foreign id (if set) will be missing from the event with id: {}.",
                        event.getNodeid(), event.getDbid(), e);
                // We only include the node id in the node criteria in when forwarding events
                // since the event does not currently contain the fs:fid or a reference to the node object.
                builder.setNodeCriteria(OpennmsModelProtos.NodeCriteria.newBuilder()
                        .setId(event.getNodeid()));
            }
        }

        getString(event.getInterface()).ifPresent(builder::setIpAddress);

        for (Parm parm : event.getParmCollection()) {
            if (parm.getParmName() == null || parm.getValue() == null) {
                continue;
            }
            String value = parm.getValue().getContent() == null ? "" : parm.getValue().getContent();
            builder.addParameter(OpennmsModelProtos.EventParameter.newBuilder()
                    .setName(parm.getParmName())
                    .setValue(value));

        }

        setTimeIfNotNull(event.getTime(), builder::setTime);
        setTimeIfNotNull(event.getCreationTime(), builder::setCreateTime);

        return builder;
    }

    public OpennmsModelProtos.Event.Builder toEvent(OnmsEvent event) {
        if (event == null) {
            return null;
        }
        try {
            final OpennmsModelProtos.Event.Builder builder = OpennmsModelProtos.Event.newBuilder()
                    .setId(event.getId())
                    .setUei(event.getEventUei())
                    .setSource(event.getEventSource())
                    .setSeverity(toSeverity(OnmsSeverity.get(event.getEventSeverity())))
                    .setLog("Y".equalsIgnoreCase(event.getEventLog()))
                    .setDisplay("Y".equalsIgnoreCase(event.getEventDisplay()));

            final String eventLabel = eventConfDao.getEventLabel(event.getEventUei());
            if (eventLabel != null) {
                builder.setLabel(eventLabel);
            }
            if (event.getEventDescr() != null) {
                builder.setDescription(event.getEventDescr());
            }
            if (event.getEventLogMsg() != null) {
                builder.setLogMessage(event.getEventLogMsg());
            }
            if (event.getNodeId() != null) {
                builder.setNodeCriteria(toNodeCriteria(event.getNode()));
            }

            if(event.getIpAddr() != null) {
                builder.setIpAddress(InetAddressUtils.toIpAddrString(event.getIpAddr()));
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
        } catch (RuntimeException e) {
            // We are only interested in catching org.hibernate.ObjectNotFoundExceptions, but this code runs in OSGi
            // which has a different class for this loaded then what is being thrown
            // Resort to comparing the name instead
            if (ObjectNotFoundException.class.getCanonicalName().equals(e.getClass().getCanonicalName())) {
                LOG.debug("Event was deleted before we could perform the mapping.");
                return null;
            } else {
                // Rethrow
                throw e;
            }
        }
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
        final OpennmsModelProtos.Event.Builder event = toEvent(alarm.getLastEvent());
        if (event != null) {
            builder.setLastEvent(event);
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
        if (alarm.getManagedObjectInstance() != null) {
            builder.setManagedObjectInstance(alarm.getManagedObjectInstance());
        }
        if (alarm.getManagedObjectType() != null) {
            builder.setManagedObjectType(alarm.getManagedObjectType());
        }

        if (alarm.getRelatedAlarms() != null) {
            alarm.getRelatedAlarms().forEach(relatedAlarm -> builder.addRelatedAlarm(toAlarm(relatedAlarm)));
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
        getString(alarm.getTTicketId()).ifPresent(builder::setTroubleTicketId);
        if(alarm.getTTicketState() != null) {
            builder.setTroubleTicketStateValue(alarm.getTTicketState().getValue());
        }
        setTimeIfNotNull(alarm.getFirstEventTime(), builder::setFirstEventTime);
        setTimeIfNotNull(alarm.getLastEventTime(), builder::setLastEventTime);
        setTimeIfNotNull(alarm.getAckTime(), builder::setAckTime);

        return builder;
    }

    public OpennmsModelProtos.AlarmFeedback.Builder toAlarmFeedback(AlarmFeedback alarmFeedback) {
        return OpennmsModelProtos.AlarmFeedback.newBuilder()
                .setSituationKey(alarmFeedback.getSituationKey())
                .setSituationFingerprint(alarmFeedback.getSituationFingerprint())
                .setAlarmKey(alarmFeedback.getAlarmKey())
                .setFeedbackType(OpennmsModelProtos.AlarmFeedback.FeedbackType
                        .valueOf(alarmFeedback.getFeedbackType().toString()))
                .setReason(alarmFeedback.getReason())
                .setUser(alarmFeedback.getUser())
                .setTimestamp(alarmFeedback.getTimestamp());
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

    public OpennmsModelProtos.TopologyRef.Builder toTopologyRef(OnmsTopologyProtocol protocol, String id) {
        return OpennmsModelProtos.TopologyRef.newBuilder()
                .setId(id)
                .setProtocol(Enums.getIfPresent(OpennmsModelProtos.TopologyRef.Protocol.class, protocol.getId()).orNull());
    }

    private OpennmsModelProtos.TopologyRef getTopologyRef(OnmsTopologyProtocol protocol, String id) {
        return toTopologyRef(protocol, id).build();
    }

    private OpennmsModelProtos.TopologyPort getPort(org.opennms.netmgt.topologies.service.api.OnmsTopologyPort port) {
        final OpennmsModelProtos.TopologyPort.Builder builder = OpennmsModelProtos.TopologyPort.newBuilder();
        if(port.getVertex().getId() != null) {
                builder.setVertexId(port.getVertex().getId());
        }

        if (port.getIfindex() != null) {
            builder.setIfIndex(port.getIfindex());
        }

        try {
            builder.setNodeCriteria(nodeIdToCriteriaCache.get(Integer.toUnsignedLong(port.getVertex().getNodeid())));
        } catch (CacheLoader.InvalidCacheLoadException | ExecutionException e) {
            LOG.warn("An error occurred when building node criteria for node with id: {}." +
                            " The node foreign source and foreign id (if set) will be missing from the vertex with " +
                            "id: {}.",
                    port.getVertex().getNodeid(), port.getVertex().getId(), e);
            builder.setNodeCriteria(OpennmsModelProtos.NodeCriteria.newBuilder()
                    .setId(port.getVertex().getNodeid()));
        }

        // The ifName and address might not be set so don't set nulls on the builder since protobuf does not allow null
        // values
        if (port.getIfname() != null) {
            builder.setIfName(port.getIfname());
        }

        if (port.getAddr() != null) {
            builder.setAddress(port.getAddr());
        }

        return builder.build();
    }

    private OpennmsModelProtos.TopologySegment getSegment(org.opennms.netmgt.topologies.service.api.OnmsTopologyPort port,
                                                          OnmsTopologyProtocol protocol) {
        return OpennmsModelProtos.TopologySegment.newBuilder()
                .setRef(getTopologyRef(protocol, port.getId()))
                .build();
    }

    private OpennmsModelProtos.Node getNode(org.opennms.netmgt.topologies.service.api.OnmsTopologyPort port) {
        OpennmsModelProtos.Node.Builder nodeBuilder = OpennmsModelProtos.Node.newBuilder();

        if (port.getVertex().getNodeid() != null) {
            nodeBuilder.setId(port.getVertex().getNodeid());
        }

        try {
            OpennmsModelProtos.NodeCriteria nodeCriteria =
                    nodeIdToCriteriaCache.get(Integer.toUnsignedLong(port.getVertex().getNodeid()));
            if (nodeCriteria != null) {
                nodeBuilder.setForeignSource(nodeCriteria.getForeignSource());
                nodeBuilder.setForeignId(nodeCriteria.getForeignId());
            }
        } catch (Exception ignore) {
        }

        return nodeBuilder.build();
    }

    public OpennmsModelProtos.TopologyEdge toEdgeTopologyMessage(OnmsTopologyProtocol protocol,
                                                                 org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge edge) {

        OpennmsModelProtos.TopologyEdge.Builder edgeBuilder = OpennmsModelProtos.TopologyEdge.newBuilder();
        edgeBuilder.setRef(getTopologyRef(protocol, edge.getId()));

        // Set the source
        if (edge.getSource().getVertex().getNodeid() == null) {
            // Source is a segment
            edgeBuilder.setSourceSegment(getSegment(edge.getSource(), protocol));
        } else if (edge.getSource().getIfindex() != null && edge.getSource().getIfindex() >= 0) {
            // Source is a port
            edgeBuilder.setSourcePort(getPort(edge.getSource()));
        } else {
            // Source is a node
            edgeBuilder.setSourceNode(getNode(edge.getSource()));
        }

        // Set the target
        if (edge.getTarget().getVertex().getNodeid() == null) {
            // Target is a segment
            edgeBuilder.setTargetSegment(getSegment(edge.getTarget(), protocol));
        } else if (edge.getTarget().getIfindex() != null && edge.getTarget().getIfindex() >= 0) {
            // Target is a port
            edgeBuilder.setTargetPort(getPort(edge.getTarget()));
        } else {
            // Target is a node
            edgeBuilder.setTargetNode(getNode(edge.getTarget()));
        }
        
        return edgeBuilder.build();
    }

    private static Optional<String> getString(String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

}
