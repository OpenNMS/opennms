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

import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class EventConfSourceDaoHibernate
        extends AbstractDaoHibernate<EventConfSource, Long>
        implements EventConfSourceDao {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfSourceDaoHibernate.class);

    public EventConfSourceDaoHibernate() {
        super(EventConfSource.class);
    }

    @Override
    public EventConfSource get(Long id) {
        return super.get(id);
    }

    @Override
    public EventConfSource findByName(String name) {
        List<EventConfSource> list = find("from EventConfSource s where s.name = ?", name);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<EventConfSource> findAllEnabled() {
        return find("from EventConfSource s where s.enabled = true order by s.fileOrder");
    }

    @Override
    public List<EventConfSource> findByVendor(String vendor) {
        return find("from EventConfSource s where s.vendor = ?", vendor);
    }

    @Override
    public List<EventConfSource> findAllByFileOrder() {
        return find("from EventConfSource s order by s.fileOrder");
    }

    @Override
    public Map<Long, String> getIdToNameMap() {
        return findObjects(Object[].class,
                "select s.id, s.name from EventConfSource s").stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (String) row[1]
                ));
    }

    @Override
    public void deleteAll(final Collection<EventConfSource> list) {
        super.deleteAll(list);
    }

    @Override
    public void updateEnabledFlag(Collection<Long> sourceIds, boolean enabled, boolean cascadeToEvents) {
        if (sourceIds == null || sourceIds.isEmpty()) {
            return;
        }
        String hqlSource = "update EventConfSource s set s.enabled = :enabled where s.id in (:ids)";
        getSessionFactory().getCurrentSession()
                .createQuery(hqlSource)
                .setParameter("enabled", enabled)
                .setParameterList("ids", sourceIds)
                .executeUpdate();

        if (cascadeToEvents) {
            String hqlEvents = "update EventConfEvent e set e.enabled = :enabled where e.source.id in (:ids)";
            getSessionFactory().getCurrentSession()
                    .createQuery(hqlEvents)
                    .setParameter("enabled", enabled)
                    .setParameterList("ids", sourceIds)
                    .executeUpdate();
        }

        LOG.info("Set enabled={} for sources {} (cascadeToEvents={})", enabled, sourceIds, cascadeToEvents);
    }

    @Override
    public Map<String, Object> filterEventConfSource(final String filter, final String sortBy, final String order,
                                                     final Integer totalRecords, final Integer offset, Integer limit) {

        int resultCount = (totalRecords != null) ? totalRecords : 0;
        List<EventConfSource> eventConfSourceList = Collections.emptyList();
        try {
            List<Object> queryParams = new ArrayList<>();
            List<String> conditions = new ArrayList<>();

            // Add filter conditions dynamically
            if (filter != null && !filter.trim().isEmpty()) {
                String escapedFilter = "%" + escapeLike(filter.trim().toLowerCase()) + "%";
                conditions.add("lower(s.name) like ? escape '\\'");
                queryParams.add(escapedFilter);

                conditions.add("lower(s.vendor) like ? escape '\\'");
                queryParams.add(escapedFilter);

                conditions.add("lower(s.description) like ? escape '\\'");
                queryParams.add(escapedFilter);

                conditions.add("exists (select 1 from EventConfEvent e where e.source = s and lower(e.uei) like ? escape '\\')");
                queryParams.add(escapedFilter);

            }

            String whereClause = conditions.isEmpty() ? "" : " where " + String.join(" OR ", conditions);

            // COUNT QUERY: get total matching records if not already provided
            if (resultCount == 0) {
                String countQuery = "select count(s.id) from EventConfSource s " + whereClause;
                resultCount = super.queryInt(countQuery, queryParams.toArray());
            }

            // DATA QUERY: fetch paginated results
            if (resultCount > 0) {

                String orderBy = "";
                String sortField = sortBy;

                String sortOrder = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

                Set<String> allowedSortFields = Set.of("name", "vendor", "description", "fileOrder", "eventCount");

                if (!allowedSortFields.contains(sortBy)) {
                    sortField = "createdTime";
                }

                orderBy = " order by " + sortField + " " + sortOrder;

                String dataQuery = "from EventConfSource s " + whereClause + orderBy;
                eventConfSourceList = findWithPagination(dataQuery, queryParams.toArray(), offset, limit);
            }

        } catch (Exception e ) {
            LOG.debug("Error filterEventConfSource method while fetching the records {} ", e);
        }

        // Return map with results
        return Map.of("totalRecords", resultCount, "eventConfSourceList", eventConfSourceList);

    }

    @Override
    public Integer findMaxFileOrder() {
        Integer maxOrder = (Integer) getSessionFactory().getCurrentSession()
                .createQuery("SELECT MAX(e.fileOrder) FROM EventConfSource e")
                .uniqueResult();

        return maxOrder != null ? maxOrder : 0;
    }


    @Override
    public void deleteBySourceIds(List<Long> sourceIds) {
        int deletedCount = getHibernateTemplate().execute(session ->
                session.createQuery("delete from EventConfSource s where s.id in (:ids)")
                        .setParameterList("ids", sourceIds)
                        .executeUpdate()
        );
        LOG.info("Deleted {} EventConfSource(s) with IDs: {}", deletedCount, sourceIds);
    }
    @Override
    public List<String> findAllNames() {
        return findObjects(
                String.class,
                "select distinct s.name from EventConfSource s "

        );
    }



    @Override
    public void saveOrUpdate(EventConfSource source) {
        super.saveOrUpdate(source);
    }

    @Override
    public void delete(EventConfSource source) {
        super.delete(source);
    }

    /**
     * Escapes special characters (%, _, \, /, [, ]) in a string
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
