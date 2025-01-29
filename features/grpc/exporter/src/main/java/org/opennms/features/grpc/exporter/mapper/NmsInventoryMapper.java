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
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import org.opennms.integration.api.v1.model.IpInterface;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opennms.plugin.grpc.proto.services.NmsInventoryUpdateList;
import org.opennms.plugin.grpc.proto.services.Node;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
        )
public interface NmsInventoryMapper {
    NmsInventoryMapper INSTANCE = Mappers.getMapper(NmsInventoryMapper.class);

    default Node toInventoryUpdate(org.opennms.integration.api.v1.model.Node node){

        Node.Builder nodeBuilder = Node.newBuilder()
                .setForeignId(node.getForeignId())
                .setForeignSource(node.getForeignSource())
                .setLabel(node.getLabel())
                .setLocation(node.getLocation())
                .setId(node.getId());

        List<org.opennms.plugin.grpc.proto.services.IpInterface> ipInterfaceList = node.getIpInterfaces().stream()
                .map(ipInterface -> org.opennms.plugin.grpc.proto.services.IpInterface.newBuilder()
                        .setIpAddress(ipInterface.getIpAddress().getHostAddress())
                        .build())
                .collect(Collectors.toList());
        nodeBuilder.addAllIpInterface(ipInterfaceList);

        List<org.opennms.plugin.grpc.proto.services.SnmpInterface> snmpInterfaceList = node.getSnmpInterfaces().stream()
                .map(snmpInterface -> org.opennms.plugin.grpc.proto.services.SnmpInterface.newBuilder()
                        .setIfIndex(snmpInterface.getIfIndex())
                        .setIfDescr(snmpInterface.getIfDescr())
                        .setIfName(snmpInterface.getIfName())
                        .build())
                .collect(Collectors.toList());
        nodeBuilder.addAllSnmpInterface(snmpInterfaceList);

        return nodeBuilder.build();
    }

    @Mapping(target = "instanceId", source = "runtimeInfo.systemId")
    @Mapping(target = "instanceName", source = "instanceName" )
    @Mapping(target = "nodes", source = "inventoryUpdates")
    NmsInventoryUpdateList toInventoryUpdates(final List<org.opennms.integration.api.v1.model.Node> inventoryUpdates, final RuntimeInfo runtimeInfo, final String instanceName, final boolean snapshot);
    default NmsInventoryUpdateList toInventoryUpdatesList(final List<org.opennms.integration.api.v1.model.Node> inventoryUpdates, final RuntimeInfo runtimeInfo, final String instanceName, final boolean snapshot) {
        NmsInventoryUpdateList.Builder builder = toInventoryUpdates(inventoryUpdates, runtimeInfo, instanceName,snapshot).toBuilder();
        return builder.build();
    }

    default List<org.opennms.plugin.grpc.proto.services.IpInterface> mapIpInterfaces(List<IpInterface> listIpInterface) {
        List<org.opennms.plugin.grpc.proto.services.IpInterface> list = new ArrayList<>();
        return list;
    }

}
