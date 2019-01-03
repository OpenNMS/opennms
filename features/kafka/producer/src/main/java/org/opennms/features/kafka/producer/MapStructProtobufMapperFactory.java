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

import java.util.Objects;

import org.mapstruct.factory.Mappers;
import org.opennms.core.utils.UniMapper;
import org.opennms.features.kafka.producer.mapping.AlarmFeedbackMapper;
import org.opennms.features.kafka.producer.mapping.AlarmMapper;
import org.opennms.features.kafka.producer.mapping.EventMapper;
import org.opennms.features.kafka.producer.mapping.MappingContext;
import org.opennms.features.kafka.producer.mapping.NodeMapper;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * An implementation of {@link ProtobufMapperFactory} that uses MapStruct to perform the mapping.
 */
public class MapStructProtobufMapperFactory implements ProtobufMapperFactory, MappingContext {
    private final EventConfDao eventConfDao;
    private final TransactionOperations transactionOperations;
    private final NodeDao nodeDao;
    private final HwEntityDao hwEntityDao;
    private final LoadingCache<Long, OpennmsModelProtos.NodeCriteria> nodeIdToCriteriaCache;

    public MapStructProtobufMapperFactory(EventConfDao eventConfDao, HwEntityDao hwEntityDao,
                                          TransactionOperations transactionOperations,
                                          NodeDao nodeDao, long nodeIdToCriteriaMaxCacheSize) {
        this.eventConfDao = Objects.requireNonNull(eventConfDao);
        this.hwEntityDao = Objects.requireNonNull(hwEntityDao);
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
        this.nodeDao = Objects.requireNonNull(nodeDao);

        nodeIdToCriteriaCache = CacheBuilder.newBuilder()
                .maximumSize(nodeIdToCriteriaMaxCacheSize)
                .build(new CacheLoader<Long, OpennmsModelProtos.NodeCriteria>() {
                    public OpennmsModelProtos.NodeCriteria load(Long nodeId) {
                        return transactionOperations.execute(status -> {
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

    @Override
    public UniMapper<OnmsNode, OpennmsModelProtos.Node.Builder> getNodeMapper() {
        return node -> Mappers.getMapper(NodeMapper.class).map(node, this);
    }

    @Override
    public UniMapper<Event, OpennmsModelProtos.Event.Builder> getEventMapper() {
        return event -> Mappers.getMapper(EventMapper.class).map(event, this);
    }

    @Override
    public UniMapper<OnmsAlarm, OpennmsModelProtos.Alarm.Builder> getAlarmMapper() {
        return alarm -> Mappers.getMapper(AlarmMapper.class).map(alarm, this);
    }

    @Override
    public UniMapper<AlarmFeedback, OpennmsModelProtos.AlarmFeedback.Builder> getAlarmFeedbackMapper() {
        return alarmFeedback -> Mappers.getMapper(AlarmFeedbackMapper.class).map(alarmFeedback);
    }

    @Override
    public HwEntityDao getHwEntityDao() {
        return hwEntityDao;
    }

    @Override
    public EventConfDao getEventConfDao() {
        return eventConfDao;
    }

    @Override
    public LoadingCache<Long, OpennmsModelProtos.NodeCriteria> getNodeIdToCriteriaCache() {
        return nodeIdToCriteriaCache;
    }
}
