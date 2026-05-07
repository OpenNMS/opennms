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
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.model.PageResponse;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;

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
    public SnmpCollectionMibGroup findByNameAndSource(String name, Integer snmpCollectionSourceId) {
        List<SnmpCollectionMibGroup> list = find(
                "from SnmpCollectionMibGroup s where s.name = ? and s.collectionSource.id = ?", name, snmpCollectionSourceId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SnmpCollectionMibGroup> findAllEnabled() {
        return find("from SnmpCollectionMibGroup s where s.enabled = true");
    }

    @Override
    public List<SnmpCollectionMibGroup> findAllBySource(Integer snmpCollectionSourceId) {
        return find("from SnmpCollectionMibGroup s where s.collectionSource.id = ?", snmpCollectionSourceId);
    }

    @Override
    public List<SnmpCollectionMibGroup> findAllEnabledBySource(Integer snmpCollectionSourceId) {
        return find("from SnmpCollectionMibGroup s where s.collectionSource.id = ? and s.enabled = true", snmpCollectionSourceId);
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
    public void deleteBySourceId(Integer snmpCollectionSourceId) {
        getHibernateTemplate().bulkUpdate("delete from SnmpCollectionMibGroup g where g.collectionSource.id = ?", snmpCollectionSourceId);
    }

    @Override
    public List<SnmpCollectionMibGroup> filterMibGroupConf(String name, String ifType, String vendor, String collectionSourceName, int offset, int limit) {
        List<Object> queryParamList = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("from SnmpCollectionMibGroup g where 1=1 ");
        if (name != null && !name.trim().isEmpty()) {
            queryBuilder.append(" and lower(g.name) like ? escape '\\' ");
            queryParamList.add("%" + DaoUtil.escapeLike(name.trim().toLowerCase()) + "%"); // contains match
        }

        if (ifType != null && !ifType.trim().isEmpty()) {
            queryBuilder.append(" and lower(g.ifType) like ? escape '\\' ");
            queryParamList.add("%" + DaoUtil.escapeLike(ifType.trim().toLowerCase()) + "%"); // contains match
        }

        if (vendor != null && !vendor.trim().isEmpty()) {
            queryBuilder.append(" and lower(g.collectionSource.vendor) like ? escape '\\' ");
            queryParamList.add("%" + DaoUtil.escapeLike(vendor.trim().toLowerCase()) + "%");
        }

        if (collectionSourceName != null && !collectionSourceName.trim().isEmpty()) {
            queryBuilder.append(" and lower(g.collectionSource.name) like ? escape '\\' ");
            queryParamList.add("%" + DaoUtil.escapeLike(collectionSourceName.trim().toLowerCase()) + "%");
        }

        queryBuilder.append(" order by g.name desc ");

        return findWithPagination(queryBuilder.toString(), queryParamList.toArray(), offset, limit);
    }

    @Override
    public PageResponse<SnmpCollectionMibGroup> findByCollectionSourceId(Integer snmpCollectionSourceId, String mibGroupFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit) {
        int resultCount = (totalRecords != null) ? totalRecords : 0;
        List<Object> queryParams = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        String whereClause = "where g.collectionSource.id = ? ";
        queryParams.add(snmpCollectionSourceId);

        // Add filter conditions dynamically
        if (mibGroupFilter != null && !mibGroupFilter.trim().isEmpty()) {
            String escapedFilter = "%" + DaoUtil.escapeLike(mibGroupFilter.trim().toLowerCase()) + "%";
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

       return new PageResponse<>(resultCount, mibGroupList);
    }

    @Override
    public List<String> findAllMibGroupNames() {
        return findObjects(String.class,
                "select distinct g.name from SnmpCollectionMibGroup g");
    }

    @Override
    public void updateMibGroupEnabledFlag(Integer snmpDataCollectionSourceId, List<Integer> ids, boolean enabled) {
        if (ids == null || ids.isEmpty()) {
            LOG.warn("No MIB Group IDs provided for update. Skipping...");
            return;
        }

        var session = getSessionFactory().getCurrentSession();
        String hql = " update SnmpCollectionMibGroup g set g.enabled = :enabled " +
                " where g.collectionSource.id = :sourceId and g.id in (:ids)";

        var query = session.createQuery(hql);
        query.setParameter("enabled", enabled);
        query.setParameter("sourceId", snmpDataCollectionSourceId);
        query.setParameterList("ids", ids);

        int updatedCount = query.executeUpdate();
        LOG.info("Updated {} MIB groups (enabled={}) for snmpDataCollectionSourceId={}", updatedCount, enabled, snmpDataCollectionSourceId);
    }

    @Override
    public SnmpCollectionMibGroup findBySnmpSourceCollectionIdAndId(Integer snmpCollectionSourceId, Integer id) {
        return findUnique("from SnmpCollectionMibGroup g where g.collectionSource.id = ? AND g.id = ? ", snmpCollectionSourceId, id);
    }

}
