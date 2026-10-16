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

import org.opennms.netmgt.dao.api.UserMgmtVersionDao;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class UserMgmtVersionDaoHibernate extends HibernateDaoSupport implements UserMgmtVersionDao {

    @Override
    public long getVersion(final Domain domain) {
        return query("SELECT version FROM usermgmt_version WHERE name = :name", domain);
    }

    @Override
    public long lockVersion(final Domain domain) {
        return query("SELECT version FROM usermgmt_version WHERE name = :name FOR UPDATE", domain);
    }

    private long query(final String sql, final Domain domain) {
        return getHibernateTemplate().execute(session -> {
            // write out pending changes first, so their triggers have run when the version is read
            if (session.isDirty()) {
                session.flush();
            }
            final Number version = (Number) session.createNativeQuery(sql)
                    .setParameter("name", domain.name())
                    .uniqueResult();
            return version.longValue();
        });
    }
}
