/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.systemreport.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.CountedObject;
import org.opennms.netmgt.dao.api.EventCountDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.OnmsEvent;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

public class EventCountDaoHibernate extends AbstractDaoHibernate<OnmsEvent, Integer> implements EventCountDao {

    public EventCountDaoHibernate() {
        super(OnmsEvent.class);
    }

    @Override
    public Set<CountedObject<String>> getUeiCounts(final Integer limit) {
        Set<CountedObject<String>> ueis = new TreeSet<CountedObject<String>>();
        HibernateCallback<List<CountedObject<String>>> hc = new HibernateCallback<List<CountedObject<String>>>() {
            @Override
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
