/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class GenericHibernateAccessor extends HibernateDaoSupport implements GenericPersistenceAccessor {

    protected final HibernateCriteriaConverter criteriaConverter = new HibernateCriteriaConverter();

    @Override
    public <T> List<T> find(final String query) {
        return (List<T>) getHibernateTemplate().find(query);
    }

    @Override
    public <T> List<T> find(final String query, final Object... values) {
        return (List<T>) getHibernateTemplate().find(query, values);
    }

    @Override
    public <T> List<T> findUsingNamedParameters(final String query, String[] paramNames, Object[] values) {
        return (List<T>)getHibernateTemplate().findByNamedParam(query, paramNames, values);
    }

    @Override
    public <T> T get(Class<T> entityType, int entityId) {
        return getHibernateTemplate().get(entityType, entityId);
    }

    @Override
    public List findMatching(Criteria criteria) {
        final HibernateCallback<List> callback = new HibernateCallback<List>() {
            @Override
            public List doInHibernate(final Session session) throws HibernateException, SQLException {
                final org.hibernate.Criteria hibernateCriteria = criteriaConverter.convert(criteria, session);
                return hibernateCriteria.list();
            }
        };
        return getHibernateTemplate().execute(callback);
    }
}
