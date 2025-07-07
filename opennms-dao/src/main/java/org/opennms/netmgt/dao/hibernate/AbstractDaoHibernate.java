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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Table;

import com.google.common.collect.Sets;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.metadata.ClassMetadata;
import org.opennms.core.criteria.restrictions.AllRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

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
        // AccessLock will be created on first use in the lock() method
    }

    /** {@inheritDoc} */
    @Override
    public void lock() {
        getHibernateTemplate().execute(new HibernateCallback<Void>() {
            @Override
            public Void doInHibernate(Session session) throws HibernateException {
                // Use database-level INSERT ON CONFLICT to handle race conditions
                // This avoids transaction rollback issues
                String sql = "INSERT INTO accessLocks (lockName) VALUES (?) ON CONFLICT (lockName) DO NOTHING";
                session.createNativeQuery(sql)
                    .setParameter(1, m_lockName)
                    .executeUpdate();
                
                // Now acquire the pessimistic lock
                session.get(AccessLock.class, m_lockName, LockMode.PESSIMISTIC_WRITE);
                return null;
            }
        });
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
        final HibernateCallback<List<T>> callback = new HibernateCallback<List<T>>() {
            @Override
            public List<T> doInHibernate(final Session session) throws HibernateException {
                final Query hibernateQuery = session.createQuery(query);
                for (int i = 0; i < values.length; i++) {
                    // Hibernate 5 uses 1-based parameter indexing
                    hibernateQuery.setParameter(i + 1, values[i]);
                }
                return (List<T>)hibernateQuery.list();
            }
        };
        return getHibernateTemplate().execute(callback);
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
        final HibernateCallback<List<S>> callback = new HibernateCallback<List<S>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<S> doInHibernate(final Session session) throws HibernateException {
                final Query hibernateQuery = session.createQuery(query);
                for (int i = 0; i < values.length; i++) {
                    // Hibernate 5 uses 1-based parameter indexing
                    hibernateQuery.setParameter(i + 1, values[i]);
                }
                return (List<S>)hibernateQuery.list();
            }
        };
        return getHibernateTemplate().execute(callback);
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
            public Number doInHibernate(final Session session) throws HibernateException {
            	final Query query = session.createQuery(queryString);
                for (int i = 0; i < args.length; i++) {
                    // Hibernate 5 uses 1-based parameter indexing
                    query.setParameter(i + 1, args[i]);
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
            public T doInHibernate(final Session session) throws HibernateException {
            	final Query query = session.createQuery(queryString);
                for (int i = 0; i < args.length; i++) {
                    // Hibernate 5 uses 1-based parameter indexing
                    query.setParameter(i + 1, args[i]);
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
        //Below findMultiAndMatching method supports multiAnd criteria, which is added to support
        // multiple "and" condition on same columns of a table.
        //e.g  select * from event_parameters where (eventparam1_.name= "instance" and eventparam1_.value = "node1")
        // and (eventparam1_.name= "trigger" and eventparam1_.value = "3.0")
        if(criteria.isMultipleAnd()){
            return findMultiAndMatching(criteria);
        } else {
            final HibernateCallback<List<T>> callback = buildHibernateCallback(criteria);
            return getHibernateTemplate().execute(callback);
        }
    }

    private List<T> findMultiAndMatching(final org.opennms.core.criteria.Criteria criteria){
        Set<T> allUniqueRecords = new LinkedHashSet<>();
        Collection<Restriction> allRestrictions = criteria.getRestrictions();

        //set of multiand restrictions
        Set<Restriction> multiAndRestrictionSet = allRestrictions.stream().filter(
                restriction -> restriction.getType().equals(Restriction.RestrictionType.MULTIAND)).collect(Collectors.toSet());

        //set of non multiand restrictions
        Set<Restriction>   nonMultiAndRestrictionSet =  Sets.difference(new HashSet<Restriction>(allRestrictions),multiAndRestrictionSet);

        //iterating multiand and setting nonmultiand + single multiand restriction in criteria,
        // retrieve result set, create intersection of each query executed
        multiAndRestrictionSet.stream().forEach(restriction ->{
            Collection<Restriction> allMultiAndRestrictions = ((AllRestriction) restriction).getRestrictions();
            allMultiAndRestrictions.stream().forEach(singleMultiAndRestriction ->{
                org.opennms.core.criteria.Criteria copyOfCriteria = criteria.clone();
                copyOfCriteria.setRestrictions(nonMultiAndRestrictionSet);
                copyOfCriteria.addRestriction(singleMultiAndRestriction);
                if(allUniqueRecords.isEmpty()) {
                    allUniqueRecords.addAll(getQueryResult(copyOfCriteria));
                } else {
                    allUniqueRecords.addAll(Sets.intersection(allUniqueRecords,
                            Set.copyOf(getQueryResult(copyOfCriteria))));
                }
            });
        });
        return (List<T>) Arrays.asList(allUniqueRecords.toArray());
    }

    private List<T> getQueryResult( org.opennms.core.criteria.Criteria criteria){
        try {
            final HibernateCallback<List<T>> callback = buildHibernateCallback(criteria);
            return getHibernateTemplate().execute(callback);
        } catch (Exception ex){
            LOG.error("Error in execution of query",ex);
            return Collections.emptyList();
        }
    }

    protected <T> HibernateCallback<List<T>> buildHibernateCallback(org.opennms.core.criteria.Criteria criteria) {
        return new HibernateCallback<List<T>>() {
            @Override
            public List<T> doInHibernate(final Session session) throws HibernateException {
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
            public Integer doInHibernate(final Session session) throws HibernateException {
                
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
            public List<T> doInHibernate(final Session session) throws HibernateException {
                // In Hibernate 5, we need to unwrap the session to get the actual implementation
                final Session actualSession = session.unwrap(Session.class);
            	final Criteria attachedCrit = onmsCrit.getDetachedCriteria().getExecutableCriteria(actualSession);
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
            public Integer doInHibernate(final Session session) throws HibernateException {
                // In Hibernate 5, we need to unwrap the session to get the actual implementation
                final Session actualSession = session.unwrap(Session.class);
                final Criteria attachedCrit = onmsCrit.getDetachedCriteria().getExecutableCriteria(actualSession).setProjection(Projections.rowCount());
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
        final HibernateCallback<Integer> callback = new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(final Session session) throws HibernateException {
                final Query hibernateQuery = session.createQuery(hql);
                for (int i = 0; i < values.length; i++) {
                    // Hibernate 5 uses 1-based parameter indexing
                    hibernateQuery.setParameter(i + 1, values[i]);
                }
                return hibernateQuery.executeUpdate();
            }
        };
        return getHibernateTemplate().execute(callback);
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
     * @throws DataAccessException if any.
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
                    LOG.warn("Duplicate key constraint violation, class: {}, key value: {}", m_entityClass.getName(), meta.getIdentifier(entity));
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
