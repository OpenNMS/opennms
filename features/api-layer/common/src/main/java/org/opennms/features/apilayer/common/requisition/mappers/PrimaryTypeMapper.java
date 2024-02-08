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
package org.opennms.features.apilayer.common.requisition.mappers;

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
