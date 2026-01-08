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

import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventConfEventDaoHibernate
        extends AbstractDaoHibernate<EventConfEvent, Long>
        implements EventConfEventDao {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfEventDaoHibernate.class);

    public EventConfEventDaoHibernate() {
        super(EventConfEvent.class);
    }

    @Override
    public List<EventConfEvent> findBySourceId(Long sourceId) {
        return find("from EventConfEvent e where e.source.id = ? order by e.createdTime desc", sourceId);
    }

    @Override
    public EventConfEvent findByUei(String uei) {
        List<EventConfEvent> list = find("from EventConfEvent e where e.uei = ?", uei);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<EventConfEvent> findByUeiAndSourceId(String uei, Long sourceId) {
        return find("from EventConfEvent e where e.uei = ? and e.source.id = ?", uei, sourceId);
    }

    @Override
    public int countBySourceId(Long sourceId) {
        return queryInt("select count(e.id) from EventConfEvent e where e.source.id = ?", sourceId);
    }

    public List<EventConfEvent> filterEventConf(final String uei, final String vendor, final String sourceName, final int offset, final int limit) {
        List<Object> queryParamList = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("from EventConfEvent e where 1=1 ");
        if (uei != null && !uei.trim().isEmpty()) {
            queryBuilder.append(" and lower(e.uei) like ? escape '\\' ");
            queryParamList.add("%" + escapeLike(uei.trim().toLowerCase()) + "%"); // contains match
        }

        if (vendor != null && !vendor.trim().isEmpty()) {
            queryBuilder.append(" and lower(e.source.vendor) like ? escape '\\' ");
            queryParamList.add("%" + escapeLike(vendor.trim().toLowerCase()) + "%");
        }

        if (sourceName != null && !sourceName.trim().isEmpty()) {
            queryBuilder.append(" and lower(e.source.name) like ? escape '\\' ");
            queryParamList.add("%" + escapeLike(sourceName.trim().toLowerCase()) + "%");
        }

        queryBuilder.append(" order by e.createdTime desc ");

        return findWithPagination(queryBuilder.toString(), queryParamList.toArray(), offset, limit);
    }

    @Override
    public Map<String, Object> findBySourceId(Long sourceId, String eventFilter, String eventSortBy, String eventOrder, Integer totalRecords, Integer offset, Integer limit) {

        int resultCount = (totalRecords != null) ? totalRecords : 0;
        List<Object> queryParams = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        String whereClause = "where e.source.id = ? ";
        queryParams.add(sourceId);

        // Add filter conditions dynamically
        if (eventFilter != null && !eventFilter.trim().isEmpty()) {
            String escapedFilter = "%" + escapeLike(eventFilter.trim().toLowerCase()) + "%";
            conditions.add("lower(e.uei) like ? escape '\\'");
            queryParams.add(escapedFilter);

            conditions.add("lower(e.eventLabel) like ? escape '\\'");
            queryParams.add(escapedFilter);

            conditions.add("lower(e.description) like ? escape '\\'");
            queryParams.add(escapedFilter);

        }

         whereClause = whereClause + (conditions.isEmpty() ? "" : " AND ( " + String.join(" OR ", conditions)+ ")");

        // COUNT QUERY: get total matching records if not already provided
        if (resultCount == 0) {
            String countQuery = "select count(e.id) from EventConfEvent e " + whereClause;
            resultCount = super.queryInt(countQuery, queryParams.toArray());
        }

        // DATA QUERY: fetch paginated results if resultCount > 0
        List<EventConfEvent> eventConfEventList = Collections.emptyList();
        if (resultCount > 0) {

            String orderBy = "";
            String sortField = eventSortBy;

            String sortOrder = "ASC".equalsIgnoreCase(eventOrder) ? "ASC" : "DESC";

            Set<String> allowedSortFields = Set.of("uei", "eventLabel", "description", "severity", "enabled");

            if (eventSortBy == null || !allowedSortFields.contains(eventSortBy)) {
                sortField = "createdTime";
            }

            if ("severity".equalsIgnoreCase(sortField)) {
                orderBy = " order by case upper(e.severity) " +
                        " when 'INDETERMINATE' then 1 " +
                        " when 'CLEARED' then 2 " +
                        " when 'NORMAL' then 3 " +
                        " when 'WARNING' then 4 " +
                        " when 'MINOR' then 5 " +
                        " when 'MAJOR' then 6 " +
                        " when 'CRITICAL' then 7 " +
                        " else 999 end " + sortOrder;
            } else {
                orderBy = " order by e." + sortField + " " + sortOrder;
            }


            String dataQuery = "from EventConfEvent e " + whereClause + orderBy;
            eventConfEventList = findWithPagination(dataQuery, queryParams.toArray(), offset, limit);
        }

        // Return map with results
        return Map.of("totalRecords", resultCount, "eventConfEventList", eventConfEventList);
    }

    @Override
    public void saveAll(Collection<EventConfEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        int batchSize = 50;
        int i = 0;
        for (EventConfEvent event : events) {
            getHibernateTemplate().saveOrUpdate(event);
            i++;
            if (i % batchSize == 0) {
                getHibernateTemplate().flush();
                getHibernateTemplate().clear();
            }

        }
        getHibernateTemplate().flush();
        getHibernateTemplate().clear();
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

    @Override
    public List<EventConfEvent> findEnabledEvents() {
        return find("from EventConfEvent e where e.enabled = true order by e.id asc");
    }

    @Override
    public void deleteBySourceId(Long sourceId) {
        getHibernateTemplate().bulkUpdate("delete from EventConfEvent e where e.source.id = ?", sourceId);
    }

    @Override
    public void deleteAll(final Collection<EventConfEvent> list) {
        super.deleteAll(list);
    }

    @Override
    public void updateEventEnabledFlag(Long sourceId, List<Long> eventIds, boolean enabled) {
        if (eventIds == null || eventIds.isEmpty()) {
            LOG.warn("No event IDs provided for update. Skipping...");
            return;
        }

        var session = getSessionFactory().getCurrentSession();
        String hql = "update EventConfEvent e set e.enabled = :enabled " +
                "where e.source.id = :sourceId and e.id in (:eventIds)";

        var query = session.createQuery(hql);
        query.setParameter("enabled", enabled);
        query.setParameter("sourceId", sourceId);
        query.setParameterList("eventIds", eventIds);

        int updatedCount = query.executeUpdate();
        LOG.info("Updated {} events (enabled={}) for sourceId={}", updatedCount, enabled, sourceId);
    }

    @Override
    public void deleteByEventIds(Long sourceId, List<Long> eventIds) {
        int deletedCount = getHibernateTemplate().execute(session ->
                session.createQuery("delete from EventConfEvent e where e.source.id = :sourceId and  e.id in (:ids)")
                        .setParameter("sourceId", sourceId)
                        .setParameterList("ids", eventIds)
                        .executeUpdate()
        );
        LOG.info("Deleted {} EventConfEvent(s) with IDs: {} for sourceId: {}", deletedCount, eventIds, sourceId);
    }

    @Override
    public List<EventConfEvent> findEventsByVendor(String vendor) {
        return find("from EventConfEvent e where e.enabled = true  and  e.source.vendor = ? order by e.id asc ", vendor);
    }

    @Override
    public EventConfEvent findBySourceIdAndEventId(Long sourceId, Long eventId) {
        return findUnique("from EventConfEvent e where e.source.id = ? AND  e.id = ? ", sourceId, eventId);
    }
}
