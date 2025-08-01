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
import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * <p>LinkStateDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MinionDaoHibernate extends AbstractDaoHibernate<OnmsMinion, String> implements MinionDao {
    public MinionDaoHibernate() {
        super(OnmsMinion.class);
    }

    @Override
    public Collection<OnmsMinion> findAll(final Integer offset, final Integer limit) {
        return getHibernateTemplate().execute(new HibernateCallback<Collection<OnmsMinion>>() {

            @Override
            @SuppressWarnings("unchecked")
            public Collection<OnmsMinion> doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsMinion.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }
        });
    }

    @Override
    public OnmsMinion findById(final String id) {
        return findUnique("from OnmsMinion as m where m.id = ?", id);
    }
    
    @Override
    public Collection<OnmsMinion> findByLocation(final String locationName) {
        return find("from OnmsMinion as m where m.location = ?", locationName);
    }
}
