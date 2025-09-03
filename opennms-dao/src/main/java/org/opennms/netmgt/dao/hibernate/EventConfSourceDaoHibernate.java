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
    public Integer findMaxFileOrder() {
        Integer maxOrder = (Integer) getSessionFactory().getCurrentSession()
                .createQuery("SELECT MAX(e.fileOrder) FROM EventConfSource e")
                .uniqueResult();

        return maxOrder != null ? maxOrder : 0;
    }


    @Override
    public void saveOrUpdate(EventConfSource source) {
        super.saveOrUpdate(source);
    }

    @Override
    public void delete(EventConfSource source) {
        super.delete(source);
    }
}
