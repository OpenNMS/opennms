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
