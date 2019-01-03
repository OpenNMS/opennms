/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer.mapping;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

/**
 * Used by MapStruct to generate mapping code.
 */
@Mapper(uses = {ProtoBuilderFactory.class, DateMapper.class}, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface NodeMapper {
    OpennmsModelProtos.Node.Builder map(OnmsNode node, @Context MappingContext mappingContext);

    default String mapLocationToString(OnmsMonitoringLocation location) {
        return location == null ? null : location.getLocationName();
    }

    @AfterMapping
    default void afterMapping(OnmsNode node, @MappingTarget OpennmsModelProtos.Node.Builder nodeBuilder,
                              @Context MappingContext mappingContext) {
        // Add all of the interfaces
        node.getSnmpInterfaces().forEach(s -> nodeBuilder.addSnmpInterface(Mappers.getMapper(SnmpInterfaceMapper.class)
                .map(s)));
        node.getIpInterfaces().forEach(i -> nodeBuilder.addIpInterface(Mappers.getMapper(IpInterfaceMapper.class)
                .map(i)));

        // Add all of the categories, sorting them by name first
        node.getCategories()
                .stream()
                .map(OnmsCategory::getName)
                .sorted()
                .forEach(nodeBuilder::addCategory);

        OnmsHwEntity rootEntity = mappingContext.getHwEntityDao().findRootByNodeId(node.getId());
        if (rootEntity != null) {
            nodeBuilder.setHwInventory(Mappers.getMapper(HwEntityMapper.class).map(rootEntity));
        }
    }
}
