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
import org.opennms.netmgt.model.SnmpCollectionMibGroup;

import java.util.Collection;
import java.util.List;

public interface SnmpCollectionMibGroupDao extends OnmsDao<SnmpCollectionMibGroup, Integer> {
    SnmpCollectionMibGroup get(Integer id);

    SnmpCollectionMibGroup findByNameAndSource(String name, Integer snmpCollectionSourceId);

    List<SnmpCollectionMibGroup> findAll();

    List<SnmpCollectionMibGroup> findAllEnabled();

    List<SnmpCollectionMibGroup> findAllBySource(Integer snmpCollectionSourceId);

    List<SnmpCollectionMibGroup> findAllEnabledBySource(Integer snmpCollectionSourceId);

    void saveOrUpdate(SnmpCollectionMibGroup mibGroup);

    void delete(SnmpCollectionMibGroup mibGroup);

    void deleteAll(final Collection<SnmpCollectionMibGroup> list);

    void saveAll(Collection<SnmpCollectionMibGroup> list);

    void deleteBySourceId(Integer snmpCollectionSourceId);

    List<SnmpCollectionMibGroup> filterMibGroupConf(String name,String ifType, String vendor, String collectionSourceName, int offset, int limit);

    PageResponse<SnmpCollectionMibGroup> findByCollectionSourceId(Integer snmpCollectionSourceId, String mibGroupFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit);

    SnmpCollectionMibGroup findBySnmpSourceCollectionIdAndId(Integer sourceId, Integer  id);

    List<String> findAllMibGroupNames();

    void updateMibGroupEnabledFlag(Integer snmpDataCollectionSourceId, List<Integer> ids, boolean enabled);
}

