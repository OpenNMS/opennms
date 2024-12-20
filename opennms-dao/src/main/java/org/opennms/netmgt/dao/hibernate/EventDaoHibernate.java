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

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;

public class EventDaoHibernate extends AbstractDaoHibernate<OnmsEvent, Integer> implements EventDao {

	public EventDaoHibernate() {
		super(OnmsEvent.class);
	}

    /** {@inheritDoc} */
        @Override
    public int deletePreviousEventsForAlarm(Integer id, OnmsEvent e) throws DataAccessException {
        String hql = "delete from OnmsEvent where alarmid = ? and eventid != ?";
        Object[] values = {id, e.getId()};
        return bulkDelete(hql, values);
    }

    @Override
    public int countNodesFromPast24Hours() {
        AtomicReference<Long> count = new AtomicReference<>(0L);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -24);  // Subtract 24 hours
        java.util.Date twentyFourHoursAgo = calendar.getTime();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(twentyFourHoursAgo.getTime());
        getHibernateTemplate().executeWithNativeSession(session -> {
            Query query = session.createQuery("SELECT COUNT(n.id) FROM OnmsEvent n WHERE n.eventTime >= :twentyFourHoursAgo");
            query.setParameter("twentyFourHoursAgo", timestamp);
            count.set((Long) query.uniqueResult());
            return count.get() != null ? count.get().intValue() : 0;
        });
        return count.get().intValue();
    }

    @Override
    public List<OnmsEvent> getEventsAfterDate(final List<String> ueiList, final Date date) {
        final String hql = "From OnmsEvent e where e.eventUei in (:eventUei) and e.eventTime > :eventTime order by e.eventTime desc";

        return (List<OnmsEvent>)getHibernateTemplate().executeFind(new HibernateCallback<List<OnmsEvent>>() {
            @Override
            public List<OnmsEvent> doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery(hql)
                        .setParameterList("eventUei", ueiList)
                        .setParameter("eventTime", date)
                        .list();
            }
        });
    }

    public List<OnmsEvent> getEventsForEventParameters(final Map<String, String> eventParameters) {
        final StringBuffer hqlStringBuffer = new StringBuffer("From OnmsEvent e where ");
        for (int i = 0; i < eventParameters.size(); i++) {
            if (i > 0) {
                hqlStringBuffer.append(" and ");
            }
            hqlStringBuffer.append("exists (select p.event from OnmsEventParameter p where e=p.event and p.name = :name" + i + " and p.value like :value" + i + ")");
        }

        return (List<OnmsEvent>) getHibernateTemplate().executeFind(new HibernateCallback<List<OnmsEvent>>() {
            @Override
            public List<OnmsEvent> doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.createQuery(hqlStringBuffer.toString());
                int i = 0;
                for (final Map.Entry<String, String> entry : eventParameters.entrySet()) {
                    q = q.setParameter("name" + i, entry.getKey()).setParameter("value" + i, entry.getValue());
                    i++;
                }

                return q.list();
            }
        });
    }
}
