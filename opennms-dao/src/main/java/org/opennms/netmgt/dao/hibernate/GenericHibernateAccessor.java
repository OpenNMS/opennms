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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
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
    public <T> List<T> findUsingNamedParameters(final String query, String[] paramNames, Object[] values, Integer offset, Integer limit) {
        if (limit == null && offset == null) {
            return findUsingNamedParameters(query, paramNames, values);
        }

        // This part is inspired by HibernateTemplate.findUsingNamedParameters.
        // Unfortunately it does not support limiting the query, so we do it manually
        if(paramNames.length != values.length) {
            throw new IllegalArgumentException("Length of paramNames array must match length of values array");
        }
        return getHibernateTemplate().executeWithNativeSession(session -> {
            final Query queryObject = session.createQuery(query);
            prepareQuery(queryObject);
            if (offset != null) {
                queryObject.setFirstResult(offset);
            }
            if (limit != null) {
                queryObject.setMaxResults(limit);
            }
            if (values != null) {
                for (int i = 0; i < values.length; ++i) {
                    applyNamedParameterToQuery(queryObject, paramNames[i], values[i]);
                }
            }
            return queryObject.list();
        });

    }

    @Override
    public <T> List<T> executeNativeQuery(String sql, Map<String, Object> parameterMap) {
        final List result = getHibernateTemplate().execute(session -> {
            final Query query = session.createSQLQuery(sql);
            if (parameterMap != null) {
                parameterMap.entrySet().forEach(entry -> {
                    if (entry.getValue() instanceof Collection) {
                        query.setParameterList(entry.getKey(), (Collection<?>) entry.getValue());
                    } else {
                        query.setParameter(entry.getKey(), entry.getValue());
                    }
                });
            }
            return query.list();
        });
        return result;
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

    private void prepareQuery(Query queryObject) {
        if(getHibernateTemplate().isCacheQueries()) {
            queryObject.setCacheable(true);
            if(getHibernateTemplate().getQueryCacheRegion() != null) {
                queryObject.setCacheRegion(getHibernateTemplate().getQueryCacheRegion());
            }
        }

        if(getHibernateTemplate().getFetchSize() > 0) {
            queryObject.setFetchSize(getHibernateTemplate().getFetchSize());
        }

        if(getHibernateTemplate().getMaxResults() > 0) {
            queryObject.setMaxResults(getHibernateTemplate().getMaxResults());
        }
    }

    private static void applyNamedParameterToQuery(Query queryObject, String paramName, Object value) throws HibernateException {
        if(value instanceof Collection) {
            queryObject.setParameterList(paramName, (Collection<?>)value);
        } else if(value instanceof Object[]) {
            queryObject.setParameterList(paramName, (Object[])((Object[])value));
        } else {
            queryObject.setParameter(paramName, value);
        }

    }
}
