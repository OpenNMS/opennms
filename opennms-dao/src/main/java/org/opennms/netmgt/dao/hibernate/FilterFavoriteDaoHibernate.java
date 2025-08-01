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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.FilterFavoriteDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.sql.SQLException;
import java.util.List;

public class FilterFavoriteDaoHibernate extends AbstractDaoHibernate<OnmsFilterFavorite, Integer> implements FilterFavoriteDao {

    public FilterFavoriteDaoHibernate() {
        super(OnmsFilterFavorite.class);
    }

    @Override
    public OnmsFilterFavorite findBy(final String userName, final String filterName) {
        return getHibernateTemplate().execute(new HibernateCallback<OnmsFilterFavorite>() {
            @Override
            public OnmsFilterFavorite doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from OnmsFilterFavorite f where f.username = :userName and f.name = :filterName order by f.name");
                query.setParameter("filterName", filterName);
                query.setParameter("userName", userName);
                Object result = query.uniqueResult();
                if (result == null) return null;
                return (OnmsFilterFavorite)result;
            }
        });
    }

    @Override
    public List<OnmsFilterFavorite> findBy(final String userName, final OnmsFilterFavorite.Page page) {
        return getHibernateTemplate().execute(new HibernateCallback<List<OnmsFilterFavorite>>() {
            @Override
            public List<OnmsFilterFavorite> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from OnmsFilterFavorite f where f.username = :userName and f.page = :page order by f.name");
                query.setParameter("userName", userName);
                query.setParameter("page", page);
                return query.list();
            }
        });
    }


    @Override
    public boolean existsFilter(final String userName, final String filterName, final OnmsFilterFavorite.Page page) {
        List<OnmsFilterFavorite> favorites = getHibernateTemplate().execute(new HibernateCallback<List<OnmsFilterFavorite>>() {
            @Override
            public List<OnmsFilterFavorite> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from OnmsFilterFavorite f where f.username = :userName and f.page = :page and f.name = :filterName order by f.name");
                query.setParameter("userName", userName);
                query.setParameter("page", page);
                query.setParameter("filterName", filterName);
                return query.list();
            }
        });
        return !favorites.isEmpty();
    }
}
