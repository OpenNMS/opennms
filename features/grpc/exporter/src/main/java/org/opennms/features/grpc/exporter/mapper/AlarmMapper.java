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

package org.opennms.features.grpc.exporter.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.plugin.grpc.proto.spog.Alarm;
import org.opennms.plugin.grpc.proto.spog.Severity;
import org.opennms.plugin.grpc.proto.spog.AlarmUpdateList;
import org.opennms.plugin.grpc.proto.spog.Alarm.Type;
import org.opennms.plugin.grpc.proto.spog.NodeCriteria;

import java.util.Date;
import java.util.List;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
        )
public interface AlarmMapper {
    AlarmMapper INSTANCE = Mappers.getMapper(AlarmMapper.class);

    @Mapping(target = "lastEventTime", expression = "java(mapDate(onmsAlarm.getLastEventTime()))")
    @Mapping(target = "firstEventTime", expression = "java(mapDate(onmsAlarm.getFirstEventTime()))")
    @Mapping(target = "lastUpdateTime", expression = "java(mapDate(onmsAlarm.getLastUpdateTime()))")
    @Mapping(source = "counter", target = "count")
    @Mapping(target = "type", expression = "java(mapType(onmsAlarm.getType().getId()))")
    @Mapping(target = "nodeCriteria", expression = "java(mapNodeCriteria(onmsAlarm))")
    @Mapping(target = "logMessage", source = "logMsg")
    @Mapping(target = "severity", expression = "java(mapSeverity(onmsAlarm.getSeverity().toString()))")
    @Mapping(target = "operatorInstructions", source = "operInstruct")
    @Mapping(target = "managedObjectInstance", source = "managedObjectInstance")
    @Mapping(target = "managedObjectType", source = "managedObjectType")
    @Mapping(source = "ipAddr.hostAddress", target = "ipAddress")
    Alarm toAlarmUpdate(OnmsAlarm onmsAlarm);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "instanceId", source = "runtimeInfo.systemId")
    @Mapping(target = "instanceName", source = "instanceName" )
    @Mapping(target = "alarms", source = "onmsAlarmUpdates")
    AlarmUpdateList toAlarmUpdates(final List<OnmsAlarm> onmsAlarmUpdates, final RuntimeInfo runtimeInfo, final String instanceName, final boolean snapshot);

    default AlarmUpdateList toAlarmUpdatesList(final List<OnmsAlarm> onmsAlarmUpdates, final RuntimeInfo runtimeInfo, final String instanceName, final boolean snapshot) {
       AlarmUpdateList.Builder builder = toAlarmUpdates(onmsAlarmUpdates,runtimeInfo, instanceName,snapshot).toBuilder();
        return builder.build();
    }

    @Mapping(target = "nodeLabel", source = "nodeLabel", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "location", source = "node.location.locationName", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "foreignId", source = "node.foreignId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "foreignSource", source = "node.foreignSource", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "id", source = "nodeId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    NodeCriteria mapNodeCriteria(OnmsAlarm alarm);

    default int mapType(Integer value) {
        switch (value) {
            case 0:
                return Type.PROBLEM_WITH_CLEAR.getNumber();
            case 1:
                return Type.CLEAR.getNumber();
            case 2:
                return Type.PROBLEM_WITHOUT_CLEAR.getNumber();
            default:
                throw new IllegalArgumentException("Unknown value for Type: " + value);
        }
    }

    default long mapDate(Date date)
    {
        return date != null ? date.getTime() : 0;
    }

    default int mapSeverity(String severity) {

        switch (severity) {
            case "NORMAL":
                return Severity.NORMAL.getNumber();
            case "CLEARED":
                return Severity.CLEARED.getNumber();
            case "MINOR":
                return Severity.MINOR.getNumber();
            case "WARNING":
                return Severity.WARNING.getNumber();
            case "INDETERMINATE":
                return Severity.INDETERMINATE.getNumber();
            case "MAJOR":
                return Severity.MAJOR.getNumber();
            case "CRITICAL":
                return Severity.CRITICAL.getNumber();
            default:
                return Severity.UNRECOGNIZED.getNumber();
        }
    }
}
