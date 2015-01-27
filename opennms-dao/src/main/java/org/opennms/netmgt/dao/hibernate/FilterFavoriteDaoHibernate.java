/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
