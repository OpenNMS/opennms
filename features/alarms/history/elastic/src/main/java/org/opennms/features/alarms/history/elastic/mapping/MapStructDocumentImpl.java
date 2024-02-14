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
