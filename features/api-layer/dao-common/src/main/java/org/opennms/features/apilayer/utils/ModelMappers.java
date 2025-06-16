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
package org.opennms.features.apilayer.utils;

import java.util.Objects;
import java.util.stream.Collectors;

import org.hibernate.ObjectNotFoundException;
import org.mapstruct.factory.Mappers;
import org.opennms.features.apilayer.model.mappers.AlarmFeedbackMapper;
import org.opennms.features.apilayer.model.mappers.DatabaseEventMapper;
import org.opennms.features.apilayer.model.mappers.InMemoryEventMapper;
import org.opennms.features.apilayer.model.mappers.NodeMapper;
import org.opennms.features.apilayer.model.mappers.SnmpInterfaceMapper;
import org.opennms.integration.api.v1.config.events.AlarmType;
import org.opennms.integration.api.v1.model.Alarm;
import org.opennms.integration.api.v1.model.AlarmFeedback;
import org.opennms.integration.api.v1.model.DatabaseEvent;
import org.opennms.integration.api.v1.model.EventParameter;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.integration.api.v1.model.Node;
import org.opennms.integration.api.v1.model.Severity;
import org.opennms.integration.api.v1.model.SnmpInterface;
import org.opennms.integration.api.v1.model.TopologyProtocol;
import org.opennms.integration.api.v1.model.immutables.ImmutableAlarm;
import org.opennms.integration.api.v1.ticketing.Ticket.State;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Utility functions for mapping to/from API types to OpenNMS types
 */
public class ModelMappers {
    private static final Logger LOG = LoggerFactory.getLogger(ModelMappers.class);

    private static final InMemoryEventMapper inMemoryEventMapper = Mappers.getMapper(InMemoryEventMapper.class);
    private static final DatabaseEventMapper databaseEventMapper = Mappers.getMapper(DatabaseEventMapper.class);
    private static final NodeMapper nodeMapper = Mappers.getMapper(NodeMapper.class);
    private static final SnmpInterfaceMapper snmpInterfaceMapper = Mappers.getMapper(SnmpInterfaceMapper.class);
    private static final AlarmFeedbackMapper alarmFeedbackMapper = Mappers.getMapper(AlarmFeedbackMapper.class);

    // Cache the node objects. Use these in alarms, since they may be a high volume, and nodes are expensive to create.
    // We can accept the fact that the node in the alarm is not always up-to-date
    private static final String nodeCacheSpec = System.getProperty("org.opennms.features.apilayer.mapping.nodeCacheSpec",
            "maximumSize=10000,expireAfterWrite=15m");
    private static final LoadingCache<NodeKey, Node> nodeCache = CacheBuilder.from(nodeCacheSpec)
            .build(new CacheLoader<NodeKey, Node>() {
                        @Override
                        public Node load(NodeKey nodeKey)  {
                            return nodeMapper.map(nodeKey.node);
                        }
                    });

    private ModelMappers() {}

    public static Alarm toAlarm(OnmsAlarm alarm) {
        if (alarm == null) {
            return null;
        }
        final ImmutableAlarm.Builder builder = ImmutableAlarm.newBuilder()
                .setReductionKey(alarm.getReductionKey())
                .setId(alarm.getId())
                .setManagedObjectInstance(alarm.getManagedObjectInstance())
                .setManagedObjectType(alarm.getManagedObjectType())
                .setType(AlarmType.fromId(alarm.getAlarmType()))
                .setAttributes(alarm.getDetails())
                .setSeverity(toSeverity(alarm.getSeverity()))
                .setRelatedAlarms(alarm.getRelatedAlarms()
                        .stream()
                        .map(ModelMappers::toAlarm)
                        .collect(Collectors.toList()))
                .setLogMessage(alarm.getLogMsg())
                .setDescription(alarm.getDescription())
                .setLastEventTime(alarm.getLastEventTime())
                .setFirstEventTime(alarm.getFirstEventTime())
                .setAcknowledged(alarm.isAcknowledged())
                .setTicketId(alarm.getTTicketId())
                .setTicketState(ModelMappers.toTicketState(alarm.getTTicketState()));

        try {
            if (alarm.getNode() != null) {
                builder.setNode(nodeCache.get(new NodeKey(alarm.getNode())));
            }
        } catch (Exception e) {
            LOG.warn("Failed to load node for alarm with id: {}", alarm.getId(), e);
        }

        try {
            builder.setLastEvent(toEvent(alarm.getLastEvent()));
        } catch (RuntimeException e) {
            // We are only interested in catching org.hibernate.ObjectNotFoundExceptions, but this code runs in OSGi
            // which has a different class for this loaded then what is being thrown
            // Resort to comparing the name instead
            if (ObjectNotFoundException.class.getCanonicalName().equals(e.getClass().getCanonicalName())) {
                LOG.debug("The last event for alarm with id {} was deleted before we could perform the mapping." +
                        " Last event will be null.", alarm.getId());
            } else {
                // Rethrow
                throw e;
            }
        }
        return builder.build();
    }

    public static InMemoryEvent toEvent(Event event) {
        return event == null ? null : inMemoryEventMapper.map(event);
    }

    public static Event toEvent(InMemoryEvent event) {
        final EventBuilder builder = new EventBuilder(event.getUei(), event.getSource());
        if (event.getNodeId() != null) {
            builder.setNodeid(event.getNodeId().longValue());
        }
        if (event.getSeverity() != null) {
            builder.setSeverity(OnmsSeverity.get(event.getSeverity().getId()).getLabel());
        }
        if (event.getInterface() != null) {
            builder.setInterface(event.getInterface());
        }
        if (event.getService() != null) {
            builder.setService(event.getService());
        }
        if (event.getTime() != null) {
            builder.setTime(event.getTime());
        }
        for (EventParameter p : event.getParameters()) {
            builder.setParam(p.getName(), p.getValue());
        }
        return builder.getEvent();
    }

    public static DatabaseEvent toEvent(OnmsEvent event) {
        return event == null ? null : databaseEventMapper.map(event);
    }

    public static Node toNode(OnmsNode node) {
        if (node == null) {
            return null;
        }
        final Node apiNode = nodeMapper.map(node);
        nodeCache.put(new NodeKey(node.getId()), apiNode);
        return apiNode;
    }

    public static SnmpInterface toSnmpInterface(OnmsSnmpInterface snmpInterface) {
        return snmpInterface == null ? null : snmpInterfaceMapper.map(snmpInterface);
    }

    public static Severity toSeverity(OnmsSeverity severity) {
        if (severity == null) {
            return null;
        }
        switch (severity) {
            case CLEARED:
                return Severity.CLEARED;
            case NORMAL:
                return Severity.NORMAL;
            case WARNING:
                return Severity.WARNING;
            case MINOR:
                return Severity.MINOR;
            case MAJOR:
                return Severity.MAJOR;
            case CRITICAL:
                return Severity.CRITICAL;
            default:
                return Severity.INDETERMINATE;
        }
    }
    
    public static State toTicketState(final TroubleTicketState state) {
        if (state == null) {
            return null;
        }
        switch (state) {
            case OPEN:
                return State.OPEN;
            case CLOSED:
                return State.CLOSED;
            case CANCELLED:
                return State.CANCELLED;
            default:
                LOG.warn("unable to convert {} to one of OPEN, CLOSED, or CANCELLED", state);
                return null;
        }
    }

    public static AlarmFeedback toFeedback(org.opennms.features.situationfeedback.api.AlarmFeedback feedback) {
        return feedback == null ? null : alarmFeedbackMapper.map(feedback);
    }

    public static org.opennms.features.situationfeedback.api.AlarmFeedback fromFeedback(AlarmFeedback feedback) {
        return feedback == null ? null : org.opennms.features.situationfeedback.api.AlarmFeedback.newBuilder()
                .withTimestamp(feedback.getTimestamp())
                .withAlarmKey(feedback.getAlarmKey())
                .withFeedbackType(org.opennms.features.situationfeedback.api.AlarmFeedback.FeedbackType
                        .valueOfOrUnknown(feedback.getFeedbackType().toString()))
                .withReason(feedback.getReason())
                .withSituationFingerprint(feedback.getSituationFingerprint())
                .withSituationKey(feedback.getSituationKey())
                .withUser(feedback.getUser())
                .build();
    }

    public static OnmsSeverity fromSeverity(Severity severity) {
        Objects.requireNonNull(severity);
        return OnmsSeverity.get(severity.getId());
    }
    
    public static TroubleTicketState fromTicketState(final State state) {
        switch (state) {
            case OPEN:
                return TroubleTicketState.OPEN;
            case CLOSED:
                return TroubleTicketState.CLOSED;
            case CANCELLED:
                return TroubleTicketState.CANCELLED;
            default:
                LOG.warn("unhandled OPA ticket state {}", state);
                return null;
        }
    }

    public static OnmsTopologyProtocol toOnmsTopologyProtocol(TopologyProtocol protocol) {
        return OnmsTopologyProtocol.create(protocol.name());
    }
    
    public static TopologyProtocol toTopologyProtocol(OnmsTopologyProtocol protocol) {
        return TopologyProtocol.valueOf(protocol.getId());
    }

    /**
     * Key for the node cache.
     *
     * Use the node id for equals/hashCode checks, but store the actual
     * node object in order to perform the actual map operations.
     */
    private static final class NodeKey {
        private final OnmsNode node;
        private final int nodeId;

        public NodeKey(OnmsNode node) {
            this.node = node;
            this.nodeId = node.getId();
        }

        public NodeKey(int nodeId) {
            this.node = null;
            this.nodeId = nodeId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NodeKey)) return false;
            NodeKey nodeKey = (NodeKey) o;
            return nodeId == nodeKey.nodeId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId);
        }
    }
}
