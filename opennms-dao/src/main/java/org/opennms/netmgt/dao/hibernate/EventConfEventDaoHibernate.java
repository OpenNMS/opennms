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

import java.util.List;

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
    public List<EventConfEvent> findEnabledEvents() {
        return find("from EventConfEvent e where e.enabled = true order by e.createdTime desc");
    }

    @Override
    public void deleteBySourceId(Long sourceId) {
        getHibernateTemplate().bulkUpdate("delete from EventConfEvent e where e.source.id = ?", sourceId);
    }

}
