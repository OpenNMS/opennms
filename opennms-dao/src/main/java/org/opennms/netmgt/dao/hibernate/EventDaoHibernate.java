//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.CountedObject;
import org.opennms.netmgt.model.OnmsEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * <p>EventDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class EventDaoHibernate extends AbstractDaoHibernate<OnmsEvent, Integer>
		implements EventDao {

	/**
	 * <p>Constructor for EventDaoHibernate.</p>
	 */
	public EventDaoHibernate() {
		super(OnmsEvent.class);
	}

    /** {@inheritDoc} */
    public int deletePreviousEventsForAlarm(Integer id, OnmsEvent e) throws DataAccessException {
        String hql = "delete from OnmsEvent where alarmid = ? and eventid != ?";
        Object[] values = {id, e.getId()};
        return bulkDelete(hql, values);
    }

    public Set<CountedObject<String>> getUeiCounts(final Integer limit) {
        Set<CountedObject<String>> ueis = new TreeSet<CountedObject<String>>();
        HibernateCallback<List<CountedObject<String>>> hc = new HibernateCallback<List<CountedObject<String>>>() {
            public List<CountedObject<String>> doInHibernate(Session session) throws HibernateException {
                Query queryObject = session.createQuery("SELECT event.eventUei, COUNT(event.eventUei) FROM OnmsEvent event GROUP BY event.eventUei ORDER BY COUNT(event.eventUei) desc");
                queryObject.setMaxResults(limit);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                List<CountedObject<String>> ueis = new ArrayList<CountedObject<String>>();
                @SuppressWarnings("unchecked")
                final List<Object[]> l = queryObject.list();
                for (final Object[] o : l) {
                    ueis.add(new CountedObject<String>((String)o[0], (Long)o[1]));
                }
                return ueis;
            }
        };
        ueis.addAll((List<CountedObject<String>>)getHibernateTemplate().executeWithNativeSession(hc));
        return ueis;
    }

}
