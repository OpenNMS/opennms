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
import java.util.List;
import java.util.Map;
import java.util.Collections;

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
    public Map<String, Object> findBySourceId(Long sourceId, Integer totalRecords, Integer offset, Integer limit) {
        int resultCount = (totalRecords != null) ? totalRecords : 0;
        String whereClause = "where e.source.id = ?";

        // COUNT QUERY: get total matching records if not already provided
        if (resultCount == 0) {
            String countQuery = "select count(e.id) from EventConfEvent e " + whereClause;
            resultCount = super.queryInt(countQuery, List.of(sourceId).toArray());
        }

        // DATA QUERY: fetch paginated results if resultCount > 0
        List<EventConfEvent> eventConfEventList = Collections.emptyList();
        if (resultCount > 0) {
            String orderBy = " order by e.createdTime desc";
            String dataQuery = "from EventConfEvent e " + whereClause + orderBy;
            eventConfEventList = findWithPagination(dataQuery, List.of(sourceId).toArray(), offset, limit);
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
            getHibernateTemplate().save(event);
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
        return find("from EventConfEvent e where e.enabled = true order by e.createdTime desc");
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



}
