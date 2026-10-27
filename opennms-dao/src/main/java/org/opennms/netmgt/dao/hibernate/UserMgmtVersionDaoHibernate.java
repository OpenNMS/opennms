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

import org.hibernate.Session;
import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class UserMgmtVersionDaoHibernate extends HibernateDaoSupport implements UserMgmtVersionDao {

    @Override
    public long getVersion(final Domain domain) {
        return query("SELECT version FROM usermgmt_version WHERE name = :name", domain);
    }

    @Override
    public void lockAll() {
        lock(Domain.users);
        lock(Domain.groups);
    }

    // no flush: a flush can fire the triggers, which bump the counter of a domain whose lock is not yet held
    private void lock(final Domain domain) {
        getHibernateTemplate().execute(session -> session
                .createNativeQuery("SELECT version FROM usermgmt_version WHERE name = :name FOR UPDATE")
                .setParameter("name", domain.name())
                .uniqueResult());
    }

    @Override
    public int getRowVersion(final VersionedTable table, final int id) {
        return getHibernateTemplate().execute(session -> {
            flushIfDirty(session);
            // the table name comes from the enum, so it is safe to concatenate
            final Number version = (Number) session.createNativeQuery("SELECT version FROM " + table.name() + " WHERE id = :id")
                    .setParameter("id", id)
                    .uniqueResult();
            if (version == null) {
                throw new IllegalArgumentException("No row with id " + id + " in " + table);
            }
            return version.intValue();
        });
    }

    private static void flushIfDirty(final Session session) {
        // write out pending changes first, so their triggers have run when the version is read
        if (session.isDirty()) {
            session.flush();
        }
    }

    private long query(final String sql, final Domain domain) {
        return getHibernateTemplate().execute(session -> {
            flushIfDirty(session);
            final Number version = (Number) session.createNativeQuery(sql)
                    .setParameter("name", domain.name())
                    .uniqueResult();
            return version.longValue();
        });
    }
}
