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
import org.mapstruct.factory.Mappers;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.events.api.model.ISnmp;
import org.opennms.plugin.grpc.proto.spog.EventUpdateList;
import org.opennms.plugin.grpc.proto.spog.Severity;
import org.opennms.plugin.grpc.proto.spog.Event;
import org.opennms.plugin.grpc.proto.spog.SnmpInfo;
import java.util.Date;
import java.util.List;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
        )
public interface EventsMapper {

    EventsMapper INSTANCE = Mappers.getMapper(EventsMapper.class);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "severity", expression = "java(mapSeverity(event.getSeverity()))")
    @Mapping(target = "id", source = "dbid")
    @Mapping(target = "label", source = "host")
    @Mapping(target = "createTime", source = "creationTime")
    @Mapping(target = "description", source = "descr")
    @Mapping(target = "logMessage", source = "logmsg.content")
    @Mapping(target = "ipAddress", source = "interfaceAddress.hostAddress")
    @Mapping(target = "snmpInfo", source = "snmp")
    @Mapping(target = "parameter", source = "parmCollection")
    @Mapping(target = "nodeId", source = "nodeid")
    Event mapEvent (IEvent event);

    SnmpInfo mapSnmpInfo(ISnmp snmp);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "instanceId", source = "runtimeInfo.systemId")
    @Mapping(target = "instanceName", source = "instanceName" )
    @Mapping(target = "event", source = "eventUpdates")
    @Mapping(target = "snapshot", source = "isSnapshot")
    EventUpdateList toEventUpdates(final List<IEvent> eventUpdates,
                                              final RuntimeInfo runtimeInfo, final String instanceName, final boolean isSnapshot);

    default EventUpdateList toEventUpdateList(final List<IEvent> eventUpdates,
                                              final RuntimeInfo runtimeInfo, final String instanceName, final boolean snapshot) {
        EventUpdateList.Builder builder = toEventUpdates(eventUpdates, runtimeInfo, instanceName,snapshot).toBuilder();
        return builder.build();
    }

    @Mapping(target = "name", source = "parmName")
    @Mapping(target = "value", source = "value.content")
    @Mapping(target = "type", source = "value.type")
    org.opennms.plugin.grpc.proto.spog.EventParameter mapEventParameter(IParm param);

    default long mapDate(Date date) {
        return date != null ? date.getTime() : 0;
    }

    default Severity mapSeverity(String severity) {
        switch (severity.toUpperCase()) {
            case "NORMAL":
                return Severity.NORMAL;
            case "CLEARED":
                return Severity.CLEARED;
            case "MINOR":
                return Severity.MINOR;
            case "WARNING":
                return Severity.WARNING;
            case "INDETERMINATE":
                return Severity.INDETERMINATE;
            case "MAJOR":
                return Severity.MAJOR;
            case "CRITICAL":
                return Severity.CRITICAL;
            default:
                return Severity.UNRECOGNIZED;
        }
    }
}
