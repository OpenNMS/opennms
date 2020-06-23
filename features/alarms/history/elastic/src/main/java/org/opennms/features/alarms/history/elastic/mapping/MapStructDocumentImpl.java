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

package org.opennms.features.alarms.history.elastic.mapping;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongSupplier;

import org.mapstruct.factory.Mappers;
import org.opennms.core.cache.Cache;
import org.opennms.features.alarms.history.elastic.dto.AlarmDocumentDTO;
import org.opennms.features.alarms.history.elastic.dto.AlarmDocumentFactory;
import org.opennms.features.alarms.history.elastic.dto.NodeDocumentDTO;
import org.opennms.netmgt.model.OnmsAlarm;

/**
 * A mapper that delegates mapping logic to MapStruct mappers to generate elasticsearch
 * DTO objects.
 */
public class MapStructDocumentImpl implements Function<OnmsAlarm, AlarmDocumentDTO>, AlarmDocumentFactory,
        AlarmMapper.MappingContext {
    private static final AlarmMapper alarmMapper = Mappers.getMapper(AlarmMapper.class);
    private final Cache<Integer, Optional<NodeDocumentDTO>> nodeInfoCache;
    private final LongSupplier currentTime;

    public MapStructDocumentImpl(Cache<Integer, Optional<NodeDocumentDTO>> nodeInfoCache,
                                 LongSupplier currentTime) {
        this.nodeInfoCache = nodeInfoCache;
        this.currentTime = currentTime;
    }

    @Override
    public AlarmDocumentDTO apply(OnmsAlarm alarm) {
        return alarmMapper.map(alarm, this);
    }

    @Override
    public AlarmDocumentDTO createAlarmDocumentForDelete(int alarmId, String reductionKey) {
        AlarmDocumentDTO doc = new AlarmDocumentDTO();
        doc.setId(alarmId);
        doc.setReductionKey(reductionKey);
        long now = getCurrentTime();
        doc.setUpdateTime(now);
        doc.setDeletedTime(now);

        return doc;
    }

    @Override
    public Cache<Integer, Optional<NodeDocumentDTO>> getNodeInfoCache() {
        return nodeInfoCache;
    }

    @Override
    public long getCurrentTime() {
        return currentTime.getAsLong();
    }
}
