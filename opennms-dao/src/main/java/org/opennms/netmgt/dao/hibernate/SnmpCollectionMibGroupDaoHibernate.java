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

import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SnmpCollectionMibGroupDaoHibernate extends AbstractDaoHibernate<SnmpCollectionMibGroup, Integer> implements SnmpCollectionMibGroupDao {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionMibGroupDaoHibernate.class);

    public SnmpCollectionMibGroupDaoHibernate( ) {
        super(SnmpCollectionMibGroup.class);
    }

    @Override
    public SnmpCollectionMibGroup get(Integer id) {
        return super.get(id);
    }

    @Override
    public SnmpCollectionMibGroup findByNameAndSource(String name, Integer sourceId) {
        List<SnmpCollectionMibGroup> list = find(
                "from SnmpCollectionMibGroup s where s.name = ? and s.collectionSource.id = ?", name, sourceId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SnmpCollectionMibGroup> findAllEnabled() {
        return find("from SnmpCollectionMibGroup s where s.enabled = true");
    }

    @Override
    public List<SnmpCollectionMibGroup> findAllBySource(Integer sourceId) {
        return find("from SnmpCollectionMibGroup s where s.collectionSource.id = ?", sourceId);
    }

    @Override
    public void deleteAll(final Collection<SnmpCollectionMibGroup> list) {
        super.deleteAll(list);
    }

    @Override
    public void saveAll(Collection<SnmpCollectionMibGroup> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        int batchSize = 50;
        int i = 0;
        for (SnmpCollectionMibGroup mibGroup : list) {
            getHibernateTemplate().saveOrUpdate(mibGroup);
            i++;
            if (i % batchSize == 0) {
                getHibernateTemplate().flush();
                getHibernateTemplate().clear();
            }

        }
        getHibernateTemplate().flush();
        getHibernateTemplate().clear();
    }

    @Override
    public void deleteBySourceId(Integer sourceId) {
        getHibernateTemplate().bulkUpdate("delete from SnmpCollectionMibGroup g where g.collectionSource.id = ?", sourceId);
    }

    @Override
    public List<SnmpCollectionMibGroup> filterEventConf(String name, String ifType, String vendor, String collectionSourceName, int offset, int limit) {
        List<Object> queryParamList = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("from SnmpCollectionMibGroup g where 1=1 ");
        if (name != null && !name.trim().isEmpty()) {
            queryBuilder.append(" and lower(g.name) like ? escape '\\' ");
            queryParamList.add("%" + escapeLike(name.trim().toLowerCase()) + "%"); // contains match
        }

        if (ifType != null && !ifType.trim().isEmpty()) {
            queryBuilder.append(" and lower(g.ifType) like ? escape '\\' ");
            queryParamList.add("%" + escapeLike(ifType.trim().toLowerCase()) + "%"); // contains match
        }

        if (vendor != null && !vendor.trim().isEmpty()) {
            queryBuilder.append(" and lower(t.collectionSource.vendor) like ? escape '\\' ");
            queryParamList.add("%" + escapeLike(vendor.trim().toLowerCase()) + "%");
        }

        if (collectionSourceName != null && !collectionSourceName.trim().isEmpty()) {
            queryBuilder.append(" and lower(g.collectionSource.name) like ? escape '\\' ");
            queryParamList.add("%" + escapeLike(collectionSourceName.trim().toLowerCase()) + "%");
        }

        queryBuilder.append(" order by g.createdTime desc ");

        return findWithPagination(queryBuilder.toString(), queryParamList.toArray(), offset, limit);
    }

    @Override
    public Map<String, Object> findByDataCollectionGroupId(Integer dataCollectionGroupId, String mibGroupFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit) {
        int resultCount = (totalRecords != null) ? totalRecords : 0;
        List<Object> queryParams = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        String whereClause = "where g.collectionSource.id = ? ";
        queryParams.add(dataCollectionGroupId);

        // Add filter conditions dynamically
        if (mibGroupFilter != null && !mibGroupFilter.trim().isEmpty()) {
            String escapedFilter = "%" + escapeLike(mibGroupFilter.trim().toLowerCase()) + "%";
            conditions.add("lower(g.name) like ? escape '\\'");
            queryParams.add(escapedFilter);

            conditions.add("lower(g.ifType) like ? escape '\\'");
            queryParams.add(escapedFilter);

        }

        whereClause = whereClause + (conditions.isEmpty() ? "" : " AND ( " + String.join(" OR ", conditions)+ ")");

        // COUNT QUERY: get total matching records if not already provided
        if (resultCount == 0) {
            String countQuery = "select count(g.id) from SnmpCollectionMibGroup g " + whereClause;
            resultCount = super.queryInt(countQuery, queryParams.toArray());
        }

        // DATA QUERY: fetch paginated results if resultCount > 0
        List<SnmpCollectionMibGroup> mibGroupList = Collections.emptyList();
        if (resultCount > 0) {

            String orderBy;
            String sortField = sortBy;

            String sortOrder = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

            Set<String> allowedSortFields = Set.of("name", "ifType");

            if (sortBy == null || !allowedSortFields.contains(sortBy)) {
                sortField = "name";
            }

            orderBy = " order by g." + sortField + " " + sortOrder;



            String dataQuery = "from SnmpCollectionMibGroup g " + whereClause + orderBy;
            mibGroupList = findWithPagination(dataQuery, queryParams.toArray(), offset, limit);
        }

        // Return map with results
        return Map.of("totalRecords", resultCount, "mibGroupList", mibGroupList);
    }
    /**
     * Escapes special characters (% , _ , \, ., /, [, ]) in a string
     * to make it safe for SQL LIKE queries.
     *
     * @param input the input string
     * @return the escaped string
     */
    private String escapeLike(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("@", "\\@")
                .replace("/", "\\/")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace(".", "\\.");
    }
}
