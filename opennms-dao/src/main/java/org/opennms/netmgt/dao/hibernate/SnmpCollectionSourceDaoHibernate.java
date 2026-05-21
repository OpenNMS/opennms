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
package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.DaoUtil;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.PageResponse;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class SnmpCollectionSourceDaoHibernate extends AbstractDaoHibernate<SnmpCollectionSource, Integer> implements SnmpCollectionSourceDao {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionSourceDaoHibernate.class);

    public SnmpCollectionSourceDaoHibernate() {
        super(SnmpCollectionSource.class);
    }

    @Override
    public SnmpCollectionSource get(Integer id) {
        return super.get(id);
    }

    @Override
    public SnmpCollectionSource findByName(String name) {
        List<SnmpCollectionSource> list = find("from SnmpCollectionSource s where s.name = ?", name);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SnmpCollectionSource> findAllEnabled() {
        return find("from SnmpCollectionSource s where s.enabled = true");
    }

    @Override
    public List<SnmpCollectionSource> findByUploadedBy(String uploadedBy) {
        return find("from SnmpCollectionSource s where s.uploadedBy = ?", uploadedBy);
    }

    @Override
    public void deleteAll(final Collection<SnmpCollectionSource> list) {
        super.deleteAll(list);
    }

    @Override
    public Map<Integer, String> getIdToNameMap() {
        return findObjects(Object[].class,
                "select s.id, s.name from SnmpCollectionSource s").stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (String) row[1]
                ));
    }

    @Override
    public PageResponse<SnmpCollectionSource> filterDataCollectionSource(
            final String filter,
            final String sortBy,
            final String order,
            Integer totalRecords,
            Integer offset,
            Integer limit) {

        int resultCount = totalRecords != null ? totalRecords : 0;
        List<SnmpCollectionSource> dataCollectionSourceList = Collections.emptyList();

        try {
            List<Object> queryParams = new ArrayList<>();
            List<String> conditions = new ArrayList<>();

            // Add filter conditions dynamically
            if (filter != null && !filter.isBlank()) {
                String escapedFilter =
                        "%" + DaoUtil.escapeLike(filter.trim().toLowerCase()) + "%";

                conditions.add("lower(s.name) like ? escape '\\'");
                conditions.add("lower(s.vendor) like ? escape '\\'");
                conditions.add("lower(s.description) like ? escape '\\'");

                queryParams.add(escapedFilter);
                queryParams.add(escapedFilter);
                queryParams.add(escapedFilter);
            }

            String whereClause = conditions.isEmpty()
                    ? ""
                    : " where " + String.join(" OR ", conditions);

            // COUNT QUERY: get total matching records if not already provided
            if (resultCount == 0) {
                String countQuery =
                        "select count(s.id) from SnmpCollectionSource s" + whereClause;
                resultCount = super.queryInt(countQuery, queryParams.toArray());
            }

            // DATA QUERY: fetch paginated results
            if (resultCount > 0) {
                Set<String> allowedSortFields = Set.of("name", "vendor", "description");

                String sortField =
                        (sortBy != null && !sortBy.isBlank() && allowedSortFields.contains(sortBy)) ? sortBy : "createdTime";

                String sortOrder = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

                String orderBy = " order by " + sortField + " " + sortOrder;

                String dataQuery = "from SnmpCollectionSource s" + whereClause + orderBy;

                dataCollectionSourceList = findWithPagination(dataQuery, queryParams.toArray(), offset, limit);
            }

        } catch (Exception e) {
            LOG.error("Error in filterDataCollectionSource while fetching records",e);
        }

        return new PageResponse<>(resultCount, dataCollectionSourceList);
    }
    @Override
    public void updateEnabledFlag(Collection<Integer> snmpDataCollectionSourceIds, boolean enabled) {
        if (snmpDataCollectionSourceIds == null || snmpDataCollectionSourceIds.isEmpty()) {
            return;
        }
        String hqlSources =
                "update SnmpCollectionSource s " +
                        "set s.enabled = :enabled " +
                        "where s.id in (:ids)";

        getSessionFactory().getCurrentSession()
                .createQuery(hqlSources)
                .setParameter("enabled", enabled)
                .setParameterList("ids", snmpDataCollectionSourceIds)
                .executeUpdate();

        String hqlResourceTypes =
                "update SnmpCollectionResourceType rt " +
                        "set rt.enabled = :enabled " +
                        "where rt.collectionSource.id in (:ids)";

        getSessionFactory().getCurrentSession()
                .createQuery(hqlResourceTypes)
                .setParameter("enabled", enabled)
                .setParameterList("ids", snmpDataCollectionSourceIds)
                .executeUpdate();

        String hqlMibGroups =
                "update SnmpCollectionMibGroup mg " +
                        "set mg.enabled = :enabled " +
                        "where mg.collectionSource.id in (:ids)";

        getSessionFactory().getCurrentSession()
                .createQuery(hqlMibGroups)
                .setParameter("enabled", enabled)
                .setParameterList("ids", snmpDataCollectionSourceIds)
                .executeUpdate();

        String hqlSystemDefs =
                "update SnmpCollectionSystemDef sd " +
                        "set sd.enabled = :enabled " +
                        "where sd.collectionSource.id in (:ids)";

        getSessionFactory().getCurrentSession()
                .createQuery(hqlSystemDefs)
                .setParameter("enabled", enabled)
                .setParameterList("ids", snmpDataCollectionSourceIds)
                .executeUpdate();

        LOG.info("Set enabled={} for sources {} (cascaded to collectionSources, mibGroups, resourceTypes, systemDefs)",
                enabled, snmpDataCollectionSourceIds);
    }


}



