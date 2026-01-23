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

import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;

public class SnmpCollectionResourceTypeDaoHibernate extends AbstractDaoHibernate<SnmpCollectionResourceType, Integer>  implements SnmpCollectionResourceTypeDao {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionResourceTypeDaoHibernate.class);


    public SnmpCollectionResourceTypeDaoHibernate() {
        super(SnmpCollectionResourceType.class);
    }

    @Override
    public SnmpCollectionResourceType get(Integer id) {
        return super.get(id);
    }

    @Override
    public SnmpCollectionResourceType findByNameAndSource(String name, Integer sourceId) {
        List<SnmpCollectionResourceType> list = find(
                "from SnmpCollectionResourceType t where t.name = ? and t.collectionSource.id = ?", name, sourceId);
        return list.isEmpty() ? null : list.get(0);    }

    @Override
    public List<SnmpCollectionResourceType> findAllBySource(Integer sourceId) {
        return find("from SnmpCollectionResourceType t where t.collectionSource.id = ?", sourceId);
    }

    @Override
    public List<SnmpCollectionResourceType> findAllEnabled() {
        return find("from SnmpCollectionResourceType t where t.enabled = true");
    }
    @Override
    public void deleteAll(final Collection<SnmpCollectionResourceType> list) {
        super.deleteAll(list);
    }

    @Override
    public void saveAll(Collection<SnmpCollectionResourceType> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        int batchSize = 50;
        int i = 0;
        for (SnmpCollectionResourceType resourceType : list) {
            getHibernateTemplate().saveOrUpdate(resourceType);
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
        getHibernateTemplate().bulkUpdate("delete from SnmpCollectionResourceType t where t.collectionSource.id = ?", sourceId);
    }

    @Override
    public List<SnmpCollectionResourceType> filterEventConf(String name, String label, String vendor, String collectionSourceName, int offset, int limit) {
        List<Object> queryParamList = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("from SnmpCollectionResourceType t where 1=1 ");
        if (label != null && !label.trim().isEmpty()) {
            queryBuilder.append(" and lower(t.label) like ? escape '\\' ");
            queryParamList.add("%" + escapeLike(label.trim().toLowerCase()) + "%"); // contains match
        }

        if (vendor != null && !vendor.trim().isEmpty()) {
            queryBuilder.append(" and lower(t.collectionSource.vendor) like ? escape '\\' ");
            queryParamList.add("%" + escapeLike(vendor.trim().toLowerCase()) + "%");
        }

        if (name != null && !name.trim().isEmpty()) {
            queryBuilder.append(" and lower(t.collectionSource.name) like ? escape '\\' ");
            queryParamList.add("%" + escapeLike(name.trim().toLowerCase()) + "%");
        }

        queryBuilder.append(" order by t.createdTime desc ");

        return findWithPagination(queryBuilder.toString(), queryParamList.toArray(), offset, limit);
    }

    @Override
    public Map<String, Object> findByDataCollectionGroupId(Integer dataCollectionGroupId, String resourceTypeFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit) {
        int resultCount = (totalRecords != null) ? totalRecords : 0;
        List<Object> queryParams = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        String whereClause = "where t.collectionSource.id = ? ";
        queryParams.add(dataCollectionGroupId);

        // Add filter conditions dynamically
        if (resourceTypeFilter != null && !resourceTypeFilter.trim().isEmpty()) {
            String escapedFilter = "%" + escapeLike(resourceTypeFilter.trim().toLowerCase()) + "%";
            conditions.add("lower(t.name) like ? escape '\\'");
            queryParams.add(escapedFilter);

            conditions.add("lower(t.label) like ? escape '\\'");
            queryParams.add(escapedFilter);

        }

        whereClause = whereClause + (conditions.isEmpty() ? "" : " AND ( " + String.join(" OR ", conditions)+ ")");

        // COUNT QUERY: get total matching records if not already provided
        if (resultCount == 0) {
            String countQuery = "select count(t.id) from SnmpCollectionResourceType t " + whereClause;
            resultCount = super.queryInt(countQuery, queryParams.toArray());
        }

        // DATA QUERY: fetch paginated results if resultCount > 0
        List<SnmpCollectionResourceType> resourceTypeList = Collections.emptyList();
        if (resultCount > 0) {

            String orderBy;
            String sortField = sortBy;

            String sortOrder = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

            Set<String> allowedSortFields = Set.of("name", "label");

            if (sortBy == null || !allowedSortFields.contains(sortBy)) {
                sortField = "name";
            }

            orderBy = " order by t." + sortField + " " + sortOrder;



            String dataQuery = "from SnmpCollectionResourceType t " + whereClause + orderBy;
            resourceTypeList = findWithPagination(dataQuery, queryParams.toArray(), offset, limit);
        }

        // Return map with results
        return Map.of("totalRecords", resultCount, "resourceTypeList", resourceTypeList);
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
