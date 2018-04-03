/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.persistence.Table;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.metadata.ClassMetadata;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * <p>Abstract AbstractDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractDaoHibernate<T, K extends Serializable> extends HibernateDaoSupport implements OnmsDao<T, K> {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDaoHibernate.class);
    Class<T> m_entityClass;
    private String m_lockName;
    protected final HibernateCriteriaConverter m_criteriaConverter = new HibernateCriteriaConverter();
    
    public AbstractDaoHibernate(final Class<T> entityClass) {
        super();
        m_entityClass = entityClass;
        Table table = m_entityClass.getAnnotation(Table.class);
        m_lockName = (table == null || "".equals(table.name()) ? m_entityClass.getSimpleName() : table.name()).toUpperCase() + "_ACCESS";
    }

    @Override
    protected void initDao() throws Exception {
        getHibernateTemplate().saveOrUpdate(new AccessLock(m_lockName));
    }

    /** {@inheritDoc} */
    @Override
    public void lock() {
        getHibernateTemplate().get(AccessLock.class, m_lockName, LockMode.PESSIMISTIC_WRITE);
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Object obj) {
        getHibernateTemplate().initialize(obj);
    }

    /** {@inheritDoc} */
    @Override
    public void flush() {
        getHibernateTemplate().flush();
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        getHibernateTemplate().flush(); // always flush before clearing, otherwise pending updates/saves are not executed
        getHibernateTemplate().clear();
    }

    public void merge(final T entity) {
        getHibernateTemplate().merge(entity);
    }

    /**
     * <p>find</p>
     *
     * @param query a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public List<T> find(final String query) {
        return (List<T>)getHibernateTemplate().find(query);
    }

    /**
     * <p>find</p>
     *
     * @param query a {@link java.lang.String} object.
     * @param values a {@link java.lang.Object} object.
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public List<T> find(final String query, final Object... values) {
        return (List<T>)getHibernateTemplate().find(query, values);
    }
    
    /**
     * <p>findObjects</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @param query a {@link java.lang.String} object.
     * @param values a {@link java.lang.Object} object.
     * @param <S> a S object.
     * @return a {@link java.util.List} object.
     */
    public <S> List<S> findObjects(final Class<S> clazz, final String query, final Object... values) {
        @SuppressWarnings("unchecked")
        final List<S> notifs = (List<S>)getHibernateTemplate().find(query, values);
        return notifs;
    }

    /**
     * <p>queryInt</p>
     *
     * @param query a {@link java.lang.String} object.
     * @return a int.
     */
    protected int queryInt(final String query) {
    	final HibernateCallback<Number> callback = new HibernateCallback<Number>() {
            @Override
            public Number doInHibernate(final Session session) throws HibernateException {
                return (Number)session.createQuery(query).uniqueResult();
            }
        };

        return getHibernateTemplate().execute(callback).intValue();
    }

    /**
     * <p>queryInt</p>
     *
     * @param queryString a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     * @return a int.
     */
    protected int queryInt(final String queryString, final Object... args) {
    	final HibernateCallback<Number> callback = new HibernateCallback<Number>() {
            @Override
            public Number doInHibernate(final Session session) throws HibernateException, SQLException {
            	final Query query = session.createQuery(queryString);
                for (int i = 0; i < args.length; i++) {
                    query.setParameter(i, args[i]);
                }
                return (Number)query.uniqueResult();
            }

        };

        return getHibernateTemplate().execute(callback).intValue();
    }

    /**
     * Return a single instance that matches the query string, 
     * or null if the query returns no results.
     */
    protected T findUnique(final String queryString, final Object... args) {
        final Class <? extends T> type = m_entityClass;
    	final HibernateCallback<T> callback = new HibernateCallback<T>() {
            @Override
            public T doInHibernate(final Session session) throws HibernateException, SQLException {
            	final Query query = session.createQuery(queryString);
                for (int i = 0; i < args.length; i++) {
                    query.setParameter(i, args[i]);
                }
                final Object result = query.uniqueResult();
                return result == null ? null : type.cast(result);
            }

        };
        return getHibernateTemplate().execute(callback);
    }

    /**
     * <p>countAll</p>
     *
     * @return a int.
     */
    @Override
    public int countAll() {
        return queryInt("select count(*) from " + m_entityClass.getName());
    }

    /**
     * <p>delete</p>
     *
     * @param entity a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public void delete(final T entity) throws DataAccessException {
        getHibernateTemplate().delete(entity);
    }
    
    /**
     * <p>delete</p>
     *
     * @param key a K object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public void delete(final K key) throws DataAccessException {
        delete(get(key));
    }
    
    /**
     * <p>deleteAll</p>
     *
     * @param entities a {@link java.util.Collection} object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    public void deleteAll(final Collection<T> entities) throws DataAccessException {
        getHibernateTemplate().deleteAll(entities);
    }

    /**
     * <p>findAll</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public List<T> findAll() throws DataAccessException {
        return getHibernateTemplate().loadAll(m_entityClass);
    }

    @Override
    public List<T> findMatching(final org.opennms.core.criteria.Criteria criteria) {
        final HibernateCallback<List<T>> callback = buildHibernateCallback(criteria);
        return getHibernateTemplate().execute(callback);
    }

    protected <T> HibernateCallback<List<T>> buildHibernateCallback(org.opennms.core.criteria.Criteria criteria) {
        return new HibernateCallback<List<T>>() {
            @Override
            public List<T> doInHibernate(final Session session) throws HibernateException, SQLException {
                LOG.debug("criteria = {}", criteria);
                final Criteria hibernateCriteria = m_criteriaConverter.convert(criteria, session);
                return (List<T>)(hibernateCriteria.list());
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public int countMatching(final org.opennms.core.criteria.Criteria criteria) throws DataAccessException {
        final HibernateCallback<Integer> callback = new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(final Session session) throws HibernateException, SQLException {
                
                final Criteria hibernateCriteria = m_criteriaConverter.convertForCount(criteria, session);
                hibernateCriteria.setProjection(Projections.rowCount());
                Long retval = (Long)hibernateCriteria.uniqueResult();
                hibernateCriteria.setProjection(null);
                hibernateCriteria.setResultTransformer(Criteria.ROOT_ENTITY);
                return retval.intValue();
            }
        };
        Integer retval = getHibernateTemplate().execute(callback);
        return retval == null ? 0 : retval.intValue();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<T> findMatching(final OnmsCriteria onmsCrit) throws DataAccessException {
        onmsCrit.resultsOfType(m_entityClass); //FIXME: why is this here?
        
        final HibernateCallback<List<T>> callback = new HibernateCallback<List<T>>() {
            @Override
            public List<T> doInHibernate(final Session session) throws HibernateException, SQLException {
            	final Criteria attachedCrit = onmsCrit.getDetachedCriteria().getExecutableCriteria(session);
                if (onmsCrit.getFirstResult() != null) attachedCrit.setFirstResult(onmsCrit.getFirstResult());
                if (onmsCrit.getMaxResults() != null) attachedCrit.setMaxResults(onmsCrit.getMaxResults());
                return (List<T>)attachedCrit.list();
            }
        };
        return getHibernateTemplate().execute(callback);
    }
    
    /** {@inheritDoc} */
    public int countMatching(final OnmsCriteria onmsCrit) throws DataAccessException {
        final HibernateCallback<Integer> callback = new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(final Session session) throws HibernateException, SQLException {
                final Criteria attachedCrit = onmsCrit.getDetachedCriteria().getExecutableCriteria(session).setProjection(Projections.rowCount());
                Long retval = (Long)attachedCrit.uniqueResult();
                attachedCrit.setProjection(null);
                attachedCrit.setResultTransformer(Criteria.ROOT_ENTITY);
                return retval.intValue();
            }
        };
        Integer retval = getHibernateTemplate().execute(callback);
        return retval == null ? 0 : retval.intValue();
    }
    
    /**
     * <p>bulkDelete</p>
     *
     * @param hql a {@link java.lang.String} object.
     * @param values an array of {@link java.lang.Object} objects.
     * @return a int.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    public int bulkDelete(final String hql, final Object[] values ) throws DataAccessException {
        return getHibernateTemplate().bulkUpdate(hql, values);
    }
    
    /**
     * <p>get</p>
     *
     * @param id a K object.
     * @return a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public T get(final K id) throws DataAccessException {
        return m_entityClass.cast(getHibernateTemplate().get(m_entityClass, id));
    }

    /**
     * <p>load</p>
     *
     * @param id a K object.
     * @return a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public T load(final K id) throws DataAccessException {
        return m_entityClass.cast(getHibernateTemplate().load(m_entityClass, id));
    }

    /**
     * <p>save</p>
     *
     * @param entity a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public K save(final T entity) throws DataAccessException {
        try {
            return (K)getHibernateTemplate().save(entity);
        } catch (final DataAccessException e) {
            logExtraSaveOrUpdateExceptionInformation(entity, e);
            throw e;
        }
    }

    /**
     * <p>saveOrUpdate</p>
     *
     * @param entity a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public void saveOrUpdate(final T entity) throws DataAccessException {
        try {
            getHibernateTemplate().saveOrUpdate(entity);
        } catch (final DataAccessException e) {
            logExtraSaveOrUpdateExceptionInformation(entity, e);
            throw e;
        }
    }

    /**
     * <p>update</p>
     *
     * @param entity a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public void update(final T entity) throws DataAccessException {
        try {
            getHibernateTemplate().update(entity);
        } catch (final DataAccessException e) {
            logExtraSaveOrUpdateExceptionInformation(entity, e);
            // Rethrow the exception
            throw e;
        }
    }

    /**
     * <p>Parse the {@link DataAccessException} to see if special problems were
     * encountered while performing the query. See issue NMS-5029 for examples of
     * stack traces that can be thrown from these calls.</p>
     * {@see http://issues.opennms.org/browse/NMS-5029}
     */
    private void logExtraSaveOrUpdateExceptionInformation(final T entity, final DataAccessException e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            if (cause.getMessage() != null) {
                if (cause.getMessage().contains("duplicate key value violates unique constraint")) {
                    final ClassMetadata meta = getSessionFactory().getClassMetadata(m_entityClass);
                    LOG.warn("Duplicate key constraint violation, class: {}, key value: {}", m_entityClass.getName(), meta.getPropertyValue(entity, meta.getIdentifierPropertyName(), EntityMode.POJO));
                    break;
                } else if (cause.getMessage().contains("given object has a null identifier")) {
                    LOG.warn("Null identifier on object, class: {}: {}", m_entityClass.getName(), entity.toString());
                    break;
                }
            }
            cause = cause.getCause();
        }
    }
}
