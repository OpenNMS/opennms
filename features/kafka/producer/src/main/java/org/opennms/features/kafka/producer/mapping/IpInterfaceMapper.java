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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;

/**
 * Used by MapStruct to generate mapping code.
 */
@Mapper(uses = {ProtoBuilderFactory.class, InetAddressMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface IpInterfaceMapper {
    @Mapping(source = "snmpInterface", target = "ifIndex")
    @Mapping(source = "isSnmpPrimary", target = "primaryType")
    OpennmsModelProtos.IpInterface.Builder map(OnmsIpInterface ipInterface);

    @AfterMapping
    default void afterMapping(OnmsIpInterface ipInterface,
                              @MappingTarget OpennmsModelProtos.IpInterface.Builder ipBuilder) {
        ipInterface.getMonitoredServices().forEach(svc -> ipBuilder.addService(svc.getServiceName()));
    }

    default int mapSnmpInterfaceToIfIndex(OnmsSnmpInterface snmpInterface) {
        if (snmpInterface != null && snmpInterface.getIfIndex() != null) {
            return snmpInterface.getIfIndex();
        }

        return 0;
    }

    default OpennmsModelProtos.IpInterface.PrimaryType mapPrimaryType(PrimaryType primaryType) {
        if (PrimaryType.PRIMARY.equals(primaryType)) {
            return OpennmsModelProtos.IpInterface.PrimaryType.PRIMARY;
        } else if (PrimaryType.SECONDARY.equals(primaryType)) {
            return OpennmsModelProtos.IpInterface.PrimaryType.SECONDARY;
        } else if (PrimaryType.NOT_ELIGIBLE.equals(primaryType)) {
            return OpennmsModelProtos.IpInterface.PrimaryType.NOT_ELIGIBLE;
        }

        // protobuf defaults enums to ordinal 0 rather than null
        return OpennmsModelProtos.IpInterface.PrimaryType.forNumber(0);
    }
}
