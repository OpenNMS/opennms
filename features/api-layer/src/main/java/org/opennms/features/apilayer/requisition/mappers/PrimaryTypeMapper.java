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

package org.opennms.features.apilayer.requisition.mappers;

import org.mapstruct.Mapper;
import org.opennms.integration.api.v1.config.requisition.SnmpPrimaryType;
import org.opennms.netmgt.model.PrimaryType;

@Mapper
public interface PrimaryTypeMapper {

    default org.opennms.integration.api.v1.config.requisition.SnmpPrimaryType toPrimaryType(org.opennms.netmgt.model.PrimaryType type) {
        if (type == null) {
            return null;
        }
        switch(type.getCharCode()) {
            case 'P':
                return SnmpPrimaryType.PRIMARY;
            case 'S':
                return SnmpPrimaryType.SECONDARY;
            default:
                return SnmpPrimaryType.NOT_ELIGIBLE;
        }
    }

    default org.opennms.netmgt.model.PrimaryType toSnmpPrimaryType(org.opennms.integration.api.v1.config.requisition.SnmpPrimaryType type) {
        if (type == null) {
            return null;
        }
        switch(type) {
            case PRIMARY:
                return PrimaryType.PRIMARY;
            case SECONDARY:
                return PrimaryType.SECONDARY;
            default:
                return PrimaryType.NOT_ELIGIBLE;
        }
    }

}
