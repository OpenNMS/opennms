/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
