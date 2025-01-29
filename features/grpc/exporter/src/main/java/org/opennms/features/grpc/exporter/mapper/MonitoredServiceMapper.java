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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import org.opennms.features.grpc.exporter.bsm.BsmGrpcClient;
import org.opennms.features.grpc.exporter.common.MonitoredServiceWithMetadata;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.plugin.grpc.proto.services.InventoryUpdateList;
import org.opennms.plugin.grpc.proto.services.ServiceComponent;
import org.opennms.plugin.grpc.proto.services.StateUpdate;
import org.opennms.plugin.grpc.proto.services.StateUpdateList;

import java.util.List;
import java.util.Map;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface MonitoredServiceMapper {
    MonitoredServiceMapper INSTANCE = Mappers.getMapper(MonitoredServiceMapper.class);

    @Named("foreignService")
    default String foreignService(final MonitoredServiceWithMetadata service) {
        return String.format("%s/%s/%s/%s",
                service.getNode().getForeignSource(),
                service.getNode().getForeignId(),
                service.getIface().getIpAddress().getHostAddress(),
                service.getMonitoredService().getName());
    }

    @Named("displayName")
    default String displayName(final MonitoredServiceWithMetadata service) {
        return String.format("%s %s %s",
                service.getNode().getLabel(),
                service.getIface().getIpAddress().getHostAddress(),
                service.getMonitoredService().getName());
    }

    @Named("attributes")
    default Map<String, String> attributes(final MonitoredServiceWithMetadata service) {
        return Map.ofEntries(
                Map.entry("nodeId", Integer.toString(service.getNode().getId())),
                Map.entry("nodeLabel", service.getNode().getLabel()),
                Map.entry("location", service.getNode().getLocation()),
                Map.entry("nodeCriteria", String.format("%s:%s", service.getNode().getForeignSource(), service.getNode().getForeignId())),
                Map.entry("ipAddress", service.getIface().getIpAddress().getHostAddress()),
                Map.entry("serviceName", service.getMonitoredService().getName())
        );
    }

    @Mapping(target = "foreignService", source = "service", qualifiedByName = "foreignService")
    @Mapping(target = "name", source = "service", qualifiedByName = "displayName")
    @Mapping(target = "healthy", source = "service.monitoredService.status")
    @Mapping(target = "attributes", source = "service", qualifiedByName = "attributes")
    @Mapping(target = "tags", source = "service.node.categories")
    ServiceComponent toInventoryUpdate(final MonitoredServiceWithMetadata service);

    @Mapping(target = "foreignType", constant = BsmGrpcClient.FOREIGN_TYPE)
    @Mapping(target = "foreignSource", source = "runtimeInfo.systemId")
    @Mapping(target = "services", source = "services")
    @Mapping(target = "snapshot", source = "snapshot")
    InventoryUpdateList toInventoryUpdates(final List<MonitoredServiceWithMetadata> services, final RuntimeInfo runtimeInfo, final boolean snapshot);

    @Mapping(target = "foreignService", source = "service", qualifiedByName = "foreignService")
    @Mapping(target = "healthy", source = "service.monitoredService.status")
    StateUpdate toStateUpdate(final MonitoredServiceWithMetadata service);

    @Mapping(target = "foreignType", constant = BsmGrpcClient.FOREIGN_TYPE)
    @Mapping(target = "foreignSource", source = "runtimeInfo.systemId")
    @Mapping(target = "updates", source = "updates")
    StateUpdateList toStateUpdates(final List<MonitoredServiceWithMetadata> updates, final RuntimeInfo runtimeInfo);
}
