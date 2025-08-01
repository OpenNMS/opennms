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
package org.opennms.features.apilayer.model.mappers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.opennms.core.utils.LocationUtils;
import org.opennms.integration.api.v1.model.immutables.ImmutableNode;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

@Mapper(uses = {GeolocationMapper.class, NodeAssetRecordMapper.class, IpInterfaceMapper.class,
        SnmpInterfaceMapper.class, MetaDataMapper.class, MonitoredServiceMapper.class})
public interface NodeMapper {
    ImmutableNode map(OnmsNode onmsNode);

    default String mapLocation(OnmsMonitoringLocation onmsMonitoringLocation) {
        return onmsMonitoringLocation == null ? LocationUtils.DEFAULT_LOCATION_NAME :
                LocationUtils.getEffectiveLocationName(onmsMonitoringLocation.getLocationName());
    }

    default List<String> mapCategories(Set<OnmsCategory> categories) {
        return categories == null ? Collections.emptyList() :
                categories.stream().map(OnmsCategory::getName).collect(Collectors.toList());
    }
}
