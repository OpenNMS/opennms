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
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.model.PageResponse;
import org.opennms.netmgt.model.SnmpCollectionSystemDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;

public class SnmpCollectionSystemDefDaoHibernate extends AbstractDaoHibernate<SnmpCollectionSystemDef, Integer> implements SnmpCollectionSystemDefDao {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionSystemDefDaoHibernate.class);

    public SnmpCollectionSystemDefDaoHibernate() {
        super(SnmpCollectionSystemDef.class);
    }

    @Override
    public SnmpCollectionSystemDef get(Integer id) {
        return super.get(id);
    }

    @Override
    public SnmpCollectionSystemDef findByNameAndSource(String name, Integer snmpCollectionSourceId) {
        List<SnmpCollectionSystemDef> list = find(
                "from SnmpCollectionSystemDef d where d.name = ? and d.collectionSource.id = ?", name, snmpCollectionSourceId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<SnmpCollectionSystemDef> findAllEnabled() {
        return find("from SnmpCollectionSystemDef d where d.enabled = true");

    }

    @Override
    public List<SnmpCollectionSystemDef> findAllBySource(Integer snmpCollectionSourceId) {
        return find("from SnmpCollectionSystemDef d where d.collectionSource.id = ?", snmpCollectionSourceId);
    }

    @Override
    public List<SnmpCollectionSystemDef> findAllEnabledBySource(Integer snmpCollectionSourceId) {
        return find("from SnmpCollectionSystemDef d where d.collectionSource.id = ? and d.enabled = true", snmpCollectionSourceId);
    }

    @Override
    public void deleteAll(final Collection<SnmpCollectionSystemDef> list) {
        super.deleteAll(list);
    }

    @Override
    public void saveAll(Collection<SnmpCollectionSystemDef> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        int batchSize = 50;
        int i = 0;
        for (SnmpCollectionSystemDef systemDef : list) {
            getHibernateTemplate().saveOrUpdate(systemDef);
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
        getHibernateTemplate().bulkUpdate("delete from SnmpCollectionSystemDef d where d.collectionSource.id = ?", snmpCollectionSourceId);
    }

    @Override
    public List<SnmpCollectionSystemDef> filterSystemDefsConf(String name, String vendor, String collectionSourceName, int offset, int limit) {
        List<Object> queryParamList = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("from SnmpCollectionSystemDef d where 1=1 ");
        if (name != null && !name.trim().isEmpty()) {
            queryBuilder.append(" and lower(d.name) like ? escape '\\' ");
            queryParamList.add("%" + DaoUtil.escapeLike(name.trim().toLowerCase()) + "%"); // contains match
        }

        if (vendor != null && !vendor.trim().isEmpty()) {
            queryBuilder.append(" and lower(d.collectionSource.vendor) like ? escape '\\' ");
            queryParamList.add("%" + DaoUtil.escapeLike(vendor.trim().toLowerCase()) + "%");
        }

        if (collectionSourceName != null && !collectionSourceName.trim().isEmpty()) {
            queryBuilder.append(" and lower(d.collectionSource.name) like ? escape '\\' ");
            queryParamList.add("%" + DaoUtil.escapeLike(collectionSourceName.trim().toLowerCase()) + "%");
        }

        queryBuilder.append(" order by d.name desc ");

        return findWithPagination(queryBuilder.toString(), queryParamList.toArray(), offset, limit);
    }

    @Override
    public PageResponse<SnmpCollectionSystemDef> findByCollectionSourceId(Integer snmpCollectionSourceId, String systemDefsFilter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit) {
        int resultCount = (totalRecords != null) ? totalRecords : 0;
        List<Object> queryParams = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        String whereClause = "where d.collectionSource.id = ? ";
        queryParams.add(snmpCollectionSourceId);

        // Add filter conditions dynamically
        if (systemDefsFilter != null && !systemDefsFilter.trim().isEmpty()) {
            String escapedFilter = "%" + DaoUtil.escapeLike(systemDefsFilter.trim().toLowerCase()) + "%";
            conditions.add("lower(d.name) like ? escape '\\'");
            queryParams.add(escapedFilter);


        }

        whereClause = whereClause + (conditions.isEmpty() ? "" : " AND ( " + String.join(" OR ", conditions)+ ")");

        // COUNT QUERY: get total matching records if not already provided
        if (resultCount == 0) {
            String countQuery = "select count(d.id) from SnmpCollectionSystemDef d " + whereClause;
            resultCount = super.queryInt(countQuery, queryParams.toArray());
        }

        // DATA QUERY: fetch paginated results if resultCount > 0
        List<SnmpCollectionSystemDef> systemDefsList = Collections.emptyList();
        if (resultCount > 0) {

            String orderBy;
            String sortField = sortBy;

            String sortOrder = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

            Set<String> allowedSortFields = Set.of("name");

            if (sortBy == null || !allowedSortFields.contains(sortBy)) {
                sortField = "name";
            }

            orderBy = " order by d." + sortField + " " + sortOrder;



            String dataQuery = "from SnmpCollectionSystemDef d " + whereClause + orderBy;
            systemDefsList = findWithPagination(dataQuery, queryParams.toArray(), offset, limit);
        }

        return new PageResponse<>(resultCount,systemDefsList);
    }

    @Override
    public SnmpCollectionSystemDef findBySnmpSourceCollectionIdAndId(Integer snmpCollectionSourceId, Integer id) {
        return findUnique("from SnmpCollectionSystemDef d where d.collectionSource.id = ? AND d.id = ? ", snmpCollectionSourceId, id);
    }

    @Override
    public void updateSystemDefEnabledFlag(Integer snmpDataCollectionSourceId, List<Integer> ids, boolean enabled) {
        if (ids == null || ids.isEmpty()) {
            LOG.warn("No SystemDef IDs provided for update. Skipping...");
            return;
        }

        var session = getSessionFactory().getCurrentSession();
        String hql = " update SnmpCollectionSystemDef sd set sd.enabled = :enabled " +
                " where sd.collectionSource.id = :sourceId and sd.id in (:ids)";

        var query = session.createQuery(hql);
        query.setParameter("enabled", enabled);
        query.setParameter("sourceId", snmpDataCollectionSourceId);
        query.setParameterList("ids", ids);

        int updatedCount = query.executeUpdate();
        LOG.info("Updated {} system defs (enabled={}) for snmpDataCollectionSourceId={}", updatedCount, enabled, snmpDataCollectionSourceId);
    }

}
