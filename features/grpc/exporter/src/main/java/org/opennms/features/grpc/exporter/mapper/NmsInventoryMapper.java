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
import java.util.Date;
import java.util.List;
import org.opennms.plugin.grpc.proto.spog.NmsInventoryUpdateList;
import org.opennms.plugin.grpc.proto.spog.Node;
import org.opennms.plugin.grpc.proto.spog.SnmpInterface;
import org.opennms.plugin.grpc.proto.spog.IpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
        )
public interface NmsInventoryMapper {
    static final Logger LOG = LoggerFactory.getLogger(NmsInventoryMapper.class);

    NmsInventoryMapper INSTANCE = Mappers.getMapper(NmsInventoryMapper.class);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "location", source = "node.location.locationName", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "createTime", expression = "java(mapDate(node.getCreateTime()))")
    @Mapping(target = "snmpInterface", source = "snmpInterfaces", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "ipInterface", source = "ipInterfaces", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    Node toInventoryUpdate(org.opennms.netmgt.model.OnmsNode node);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "ifPhysAddress", source = "physAddr")
    SnmpInterface mapSnmpInterface(org.opennms.netmgt.model.OnmsSnmpInterface onmsSnmpInterface );

    @Mapping(target = "primaryType", expression = "java(mapPrimaryType(onmsIpInterface))")
    @Mapping(target = "service", source = "monitoredServices")
    IpInterface mapIpInterface(org.opennms.netmgt.model.OnmsIpInterface onmsIpInterface);

    default String mapMonitoredService(org.opennms.netmgt.model.OnmsMonitoredService onmsMonitoredService){
        return onmsMonitoredService.getServiceName();
    }

    default long mapDate(Date date)
    {
        return date != null ? date.getTime() : 0;
    }

    default String mapIpAddress(java.net.InetAddress inetAddress) {
        return inetAddress != null ? inetAddress.getHostAddress() : null;
    }

    default String mapPrimaryType(org.opennms.netmgt.model.OnmsIpInterface onmsIpInterface) {
        return onmsIpInterface.getIsSnmpPrimary() != null ? onmsIpInterface.getIsSnmpPrimary().getCode() : null;
    }

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    @Mapping(target = "instanceId", source = "runtimeInfo.systemId")
    @Mapping(target = "instanceName", source = "instanceName" )
    @Mapping(target = "nodes", source = "inventoryUpdates")
    @Mapping(target = "snapshot", source = "isSnapshot")
    NmsInventoryUpdateList toInventoryUpdates(final List<org.opennms.netmgt.model.OnmsNode> inventoryUpdates,
                                              final RuntimeInfo runtimeInfo, final String instanceName, final boolean isSnapshot);

    default NmsInventoryUpdateList toInventoryUpdatesList(final List<org.opennms.netmgt.model.OnmsNode> inventoryUpdates,
                                                          final RuntimeInfo runtimeInfo, final String instanceName, final boolean snapshot) {
        NmsInventoryUpdateList.Builder builder = toInventoryUpdates(inventoryUpdates, runtimeInfo, instanceName,snapshot).toBuilder();
        return builder.build();
    }

}
