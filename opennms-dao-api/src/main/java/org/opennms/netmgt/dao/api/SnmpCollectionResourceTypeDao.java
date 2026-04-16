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
package org.opennms.netmgt.dao.api;

import org.opennms.netmgt.model.PageResponse;
import org.opennms.netmgt.model.SnmpCollectionResourceType;

import java.util.Collection;
import java.util.List;

public interface SnmpCollectionResourceTypeDao extends OnmsDao<SnmpCollectionResourceType, Integer> {
    SnmpCollectionResourceType get(Integer id);

    SnmpCollectionResourceType findByNameAndSource(String name, Integer snmpCollectionSourceId);

    List<SnmpCollectionResourceType> findAll();

    List<SnmpCollectionResourceType> findAllBySource(Integer snmpCollectionSourceId);

    List<SnmpCollectionResourceType> findAllEnabledBySource(Integer snmpCollectionSourceId);

    List<SnmpCollectionResourceType> findAllEnabled();

    void saveOrUpdate(SnmpCollectionResourceType resourceType);

    void delete(SnmpCollectionResourceType resourceType);

    void deleteAll(final Collection<SnmpCollectionResourceType> list);

    void saveAll(Collection<SnmpCollectionResourceType> list);

    void deleteBySourceId(Integer snmpCollectionSourceId);

    List<SnmpCollectionResourceType> filterResourceTypeConf(String name,String label, String vendor, String collectionSourceName, int offset, int limit);

    PageResponse<SnmpCollectionResourceType> findByCollectionSourceId(Integer snmpCollectionSourceId, String resourceTypeFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit);

    SnmpCollectionResourceType findBySnmpSourceCollectionIdAndId(Integer snmpCollectionSourceId, Integer  id);

    List<String> findAllResourceTypeNames();

    void updateResourceTypeEnabledFlag(Integer snmpDataCollectionSourceId, List<Integer> ids, boolean enabled);
}
