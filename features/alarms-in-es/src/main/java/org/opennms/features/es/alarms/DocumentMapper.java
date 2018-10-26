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

package org.opennms.features.es.alarms;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.opennms.core.cache.Cache;
import org.opennms.features.es.alarms.dto.AlarmDocumentDTO;
import org.opennms.features.es.alarms.dto.EventDocumentDTO;
import org.opennms.features.es.alarms.dto.MemoDocumentDTO;
import org.opennms.features.es.alarms.dto.NodeDocumentDTO;
import org.opennms.features.es.alarms.dto.RelatedAlarmDocumentDTO;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMemo;
import org.opennms.netmgt.model.OnmsNode;

public class DocumentMapper {

    private final Cache<Integer, Optional<NodeDocumentDTO>> nodeInfoCache;
    private final Supplier<Long> currentTime;

    public DocumentMapper(Cache<Integer, Optional<NodeDocumentDTO>> nodeInfoCache, Supplier<Long> currentTime) {
        this.nodeInfoCache = Objects.requireNonNull(nodeInfoCache);
        this.currentTime = currentTime;
    }

    public AlarmDocumentDTO toDocument(OnmsAlarm alarm) {
        final AlarmDocumentDTO document = new AlarmDocumentDTO();
        document.setId(alarm.getId());
        document.setReductionKey(alarm.getReductionKey());
        document.setFirstEventTime(alarm.getFirstEventTime().getTime());
        document.setLastEventTime(alarm.getLastEventTime().getTime());
        document.setUpdateTime(currentTime.get());
        document.setType(alarm.getAlarmType());
        document.setLogMessage(alarm.getLogMsg());
        document.setDescription(alarm.getDescription());
        document.setOperatorInstructions(alarm.getOperInstruct());
        document.setSeverityId(alarm.getSeverityId());
        document.setSeverityLabel(alarm.getSeverityLabel());
        document.setArchived(alarm.isArchived());
        document.setManagedObjectType(alarm.getManagedObjectType());
        document.setManagedObjectInstance(alarm.getManagedObjectInstance());

        // Build and set the node document - cache these
        if (alarm.getNodeId() != null) {
            final Optional<NodeDocumentDTO> cachedNodeDoc = nodeInfoCache.getIfCached(alarm.getNodeId());
            if (cachedNodeDoc != null && cachedNodeDoc.isPresent()) {
                document.setNode(cachedNodeDoc.get());
            } else {
                // We build the document here, rather than doing it in the call to the cache loader
                // since we have complete access to the node in this context, and don't want to overload the
                // cache key
                final NodeDocumentDTO nodeDoc = toNode(alarm.getNode());
                nodeInfoCache.put(alarm.getNodeId(), Optional.of(nodeDoc));
                document.setNode(nodeDoc);
            }
        }

        // Memos
        document.setStickyMemo(toMemo(alarm.getStickyMemo()));
        document.setJournalMemo(toMemo(alarm.getReductionKeyMemo()));

        // TODO: Set more fields

        // Ack
        document.setAckUser(alarm.getAckUser());
        if (alarm.getAckTime() != null) {
            document.setAckTime(alarm.getAckTime().getTime());
        }

        // Related alarms
        document.setSituation(alarm.isSituation());
        List<Integer> relatedAlarmIds = new LinkedList<>();
        List<String> relatedAlarmReductionKeys = new LinkedList<>();
        for (OnmsAlarm relatedAlarm : alarm.getRelatedAlarms()) {
            relatedAlarmIds.add(relatedAlarm.getId());
            relatedAlarmReductionKeys.add(relatedAlarm.getReductionKey());
            document.addRelatedAlarm(toRelatedAlarm(relatedAlarm));
        }
        document.setRelatedAlarmIds(relatedAlarmIds);
        document.setRelatedAlarmReductionKeys(relatedAlarmReductionKeys);

        return document;
    }

    private MemoDocumentDTO toMemo(OnmsMemo memo) {
        if (memo == null) {
            return null;
        }
        final MemoDocumentDTO doc = new MemoDocumentDTO();
        doc.setAuthor(memo.getAuthor());
        doc.setBody(memo.getBody());
        doc.setUpdateTime(memo.getUpdated() != null ? memo.getUpdated().getTime() : null);
        return doc;
    }

    private NodeDocumentDTO toNode(OnmsNode node) {
        if (node == null) {
            return null;
        }
        final NodeDocumentDTO doc = new NodeDocumentDTO();
        doc.setId(node.getId());
        doc.setLabel(node.getLabel());
        doc.setForeignId(node.getForeignId());
        doc.setForeignSource(node.getForeignSource());
        doc.setCategories(node.getCategories().stream()
                .map(OnmsCategory::getName)
                .collect(Collectors.toList()));
        return doc;
    }

    private RelatedAlarmDocumentDTO toRelatedAlarm(OnmsAlarm alarm) {
        final RelatedAlarmDocumentDTO doc = new RelatedAlarmDocumentDTO();
        doc.setId(alarm.getId());
        doc.setReductionKey(alarm.getReductionKey());
        doc.setFirstEventTime(alarm.getFirstEventTime().getTime());
        doc.setLastEventTime(alarm.getLastEventTime().getTime());
        doc.setLastEvent(toEvent(alarm.getLastEvent()));
        doc.setSeverityId(alarm.getSeverityId());
        doc.setSeverityLabel(alarm.getSeverityLabel());
        doc.setManagedObjectInstance(alarm.getManagedObjectInstance());
        doc.setManagedObjectType(alarm.getManagedObjectType());
        return doc;
    }

    private static EventDocumentDTO toEvent(OnmsEvent event) {
        final EventDocumentDTO doc = new EventDocumentDTO();
        doc.setId(event.getId());
        doc.setUei(event.getEventUei());
        doc.setLogMessage(event.getEventLogMsg());
        doc.setDescription(event.getEventDescr());
        return doc;
    }
}
