/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.persistence.Table;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.metadata.ClassMetadata;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

/**
 * <p>Abstract AbstractDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractDaoHibernate<T, K extends Serializable> implements OnmsDao<T, K>, InitializingBean {
    
    protected SessionFactory sessionFactory;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDaoHibernate.class);
    private final Class<T> m_entityClass;
    private String m_lockName;
    private final HibernateCriteriaConverter m_criteriaConverter = new HibernateCriteriaConverter();

    /**
     * <p>Constructor for AbstractDaoHibernate.</p>
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @param <K> a K object.
     */
    public AbstractDaoHibernate(final Class<T> entityClass) {
        super();
        m_entityClass = entityClass;
        Table table = m_entityClass.getAnnotation(Table.class);
        m_lockName = (table == null || "".equals(table.name()) ? m_entityClass.getSimpleName() : table.name()).toUpperCase() + "_ACCESS";
    }

    @Override
    public final void afterPropertiesSet() throws IllegalArgumentException, BeanInitializationException {
        try {
            initDao();
        }
        catch (Exception ex) {
            throw new BeanInitializationException("Initialization of DAO failed", ex);
        }
    }

    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public String getLockName() {
        return m_lockName;
    }

    protected void initDao() throws Exception {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.saveOrUpdate(new AccessLock(m_lockName));
            session.flush();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }


    /**
     * This is used to lock the table in order to implement upsert type operations
     */
    @Override
    public boolean lock() {
        return (sessionFactory.getCurrentSession().get(AccessLock.class, m_lockName, LockOptions.UPGRADE) != null);
    }


    /** {@inheritDoc} */
    @Override
    public void initialize(final Object obj) {
        Hibernate.initialize(obj);
    }

    /**
     * <p>flush</p>
     */
    @Override
    public void flush() {
        sessionFactory.getCurrentSession().flush();
    }

    /**
     * <p>clear</p>
     */
    @Override
    public void clear() {
        sessionFactory.getCurrentSession().clear();
    }

    protected int bulkUpdate(String queryString, Object... values) {
        Query queryObject = sessionFactory.getCurrentSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                queryObject.setParameter(i, values[i]);
            }
        }
        return queryObject.executeUpdate();
    }

    /**
     * <p>evict</p>
     *
     * @param entity a T object.
     */
    public void evict(final T entity) {
        sessionFactory.getCurrentSession().evict(entity);
    }

    /**
     * <p>merge</p>
     *
     * @param entity a T object.
     */
    public void merge(final T entity) {
        sessionFactory.getCurrentSession().merge(entity);
    }

    /**
     * <p>find</p>
     *
     * @param query a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public List<T> find(final String query) {
        return sessionFactory.getCurrentSession().createQuery(query).list();
    }

    /**
     * <p>find</p>
     *
     * @param query a {@link java.lang.String} object.
     * @param values a {@link java.lang.Object} object.
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public List<T> find(final String queryString, final Object... values) {
        Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        return query.list();
    }
    
    /**
     * <p>findObjects</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @param queryString a {@link java.lang.String} object.
     * @param values a {@link java.lang.Object} object.
     * @param <S> a S object.
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public <S> List<S> findObjects(final Class<S> clazz, final String queryString, final Object... values) {
        Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        return query.list();
    }

    /**
     * <p>queryInt</p>
     *
     * @param query a {@link java.lang.String} object.
     * @return a int.
     */
    protected int queryInt(final String query) {
        return ((Number)sessionFactory.getCurrentSession().createQuery(query).uniqueResult()).intValue();
    }

    /**
     * <p>queryInt</p>
     *
     * @param queryString a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     * @return a int.
     */
    protected int queryInt(final String queryString, final Object... args) {
        final Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i, args[i]);
        }
        return ((Number)query.uniqueResult()).intValue();
    }

    //TODO: This method duplicates below impl, delete this
    /**
     * <p>findUnique</p>
     *
     * @param query a {@link java.lang.String} object.
     * @return a T object.
     */
    protected T findUnique(final String query) {
        return findUnique(m_entityClass, query);
    }

    /**
     * <p>findUnique</p>
     *
     * @param queryString a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     * @return a T object.
     */
    protected T findUnique(final String queryString, final Object... args) {
        return findUnique(m_entityClass, queryString, args);
    }
    
    /**
     * <p>findUnique</p>
     *
     * @param type a {@link java.lang.Class} object.
     * @param queryString a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     * @param <S> a S object.
     * @return a S object.
     */
    protected <S> S findUnique(final Class <? extends S> type, final String queryString, final Object... args) {
        Session session = sessionFactory.getCurrentSession();
        final Query query = session.createQuery(queryString);
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i, args[i]);
        }
        final Object result = query.uniqueResult();
        return result == null ? null : type.cast(result);
    }


    /**
     * <p>countAll</p>
     *
     * @return a int.
     */
    @Override
    public long countAll() {
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
        sessionFactory.getCurrentSession().delete(entity);
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
        for (T entity : entities) {
            sessionFactory.getCurrentSession().delete(entity);
        }
    }

    /**
     * <p>findAll</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public List<T> findAll() throws DataAccessException {
        return sessionFactory.getCurrentSession().createCriteria(m_entityClass).list();
    }
    
    /**
     * <p>findMatchingObjects</p>
     *
     * @param type a {@link java.lang.Class} object.
     * @param onmsCrit a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @param <S> a S object.
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public <S> List<S> findMatchingObjects(final Class<S> type, final OnmsCriteria onmsCrit ) {
        onmsCrit.resultsOfType(type);

        final Criteria attachedCrit = onmsCrit.getDetachedCriteria().getExecutableCriteria(sessionFactory.getCurrentSession());
        if (onmsCrit.getFirstResult() != null) attachedCrit.setFirstResult(onmsCrit.getFirstResult());
        if (onmsCrit.getMaxResults() != null) attachedCrit.setMaxResults(onmsCrit.getMaxResults());
        return attachedCrit.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findMatching(final org.opennms.core.criteria.Criteria criteria) {
        LOG.debug("criteria = {}", criteria);
        final Criteria hibernateCriteria = m_criteriaConverter.convert(criteria, sessionFactory.getCurrentSession());
        return (List<T>)(hibernateCriteria.list());
    }

    /** {@inheritDoc} */
    @Override
    public long countMatching(final org.opennms.core.criteria.Criteria criteria) throws DataAccessException {
        final Criteria hibernateCriteria = m_criteriaConverter.convertForCount(criteria, sessionFactory.getCurrentSession());
        hibernateCriteria.setProjection(Projections.rowCount());
        Long retval = (Long)hibernateCriteria.uniqueResult();
        hibernateCriteria.setProjection(null);
        hibernateCriteria.setResultTransformer(Criteria.ROOT_ENTITY);
        return retval == null ? 0 : retval.intValue();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> findMatching(final OnmsCriteria onmsCrit) throws DataAccessException {
        onmsCrit.resultsOfType(m_entityClass); //FIXME: why is this here?
        final Criteria attachedCrit = onmsCrit.getDetachedCriteria().getExecutableCriteria(sessionFactory.getCurrentSession());
        if (onmsCrit.getFirstResult() != null) attachedCrit.setFirstResult(onmsCrit.getFirstResult());
        if (onmsCrit.getMaxResults() != null) attachedCrit.setMaxResults(onmsCrit.getMaxResults());
        return (List<T>)attachedCrit.list();
    }

    /** {@inheritDoc} */
    @Override
    public long countMatching(final OnmsCriteria onmsCrit) throws DataAccessException {
        final Criteria attachedCrit = onmsCrit.getDetachedCriteria().getExecutableCriteria(sessionFactory.getCurrentSession()).setProjection(Projections.rowCount());
        Long retval = (Long)attachedCrit.uniqueResult();
        attachedCrit.setProjection(null);
        attachedCrit.setResultTransformer(Criteria.ROOT_ENTITY);
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
    public int bulkDelete(final String hql, final Object... values ) throws DataAccessException {
        return bulkUpdate(hql, values);
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
        return m_entityClass.cast(sessionFactory.getCurrentSession().get(m_entityClass, id));
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
        return m_entityClass.cast(sessionFactory.getCurrentSession().load(m_entityClass, id));
    }

    /**
     * <p>save</p>
     *
     * @param entity a T object.
     * @throws org.springframework.dao.DataAccessException if any.
     */
    @Override
    public void save(final T entity) throws DataAccessException {
        sessionFactory.getCurrentSession().save(entity);
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
            sessionFactory.getCurrentSession().saveOrUpdate(entity);
        } catch (final DataAccessException e) {
            logExtraSaveOrUpdateExceptionInformation(entity, e);
            // Rethrow the exception
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
            sessionFactory.getCurrentSession().update(entity);
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
            //if (cause.getCause().getClass().getName().equals(PSQLException.class.getName())) {
            if (cause.getMessage().contains("duplicate key value violates unique constraint")) {
            	final ClassMetadata meta = getSessionFactory().getClassMetadata(m_entityClass);
                LOG.warn("Duplicate key constraint violation, class: {}, key value: {}", m_entityClass.getName(), meta.getPropertyValue(entity, meta.getIdentifierPropertyName()));
                break;
            } else if (cause.getMessage().contains("given object has a null identifier")) {
                LOG.warn("Null identifier on object, class: {}: {}", m_entityClass.getName(), entity.toString());
                break;
            }
            //}
            cause = cause.getCause();
        }
    }
}
