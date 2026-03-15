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
package org.opennms.core.daemon.common;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA-native replacement for {@code AbstractDaoHibernate}.
 *
 * <p>This base class implements {@link OnmsDao} using a JPA {@link EntityManager}
 * instead of Hibernate's {@code HibernateDaoSupport}. It is intended for use in
 * Spring Boot migrated daemons that run on Hibernate 6.x / Jakarta Persistence.</p>
 *
 * <p>Subclass DAOs (e.g. {@code AlarmDaoJpa}) extend this class and use the
 * protected helper methods ({@link #find(String, Object...)},
 * {@link #findUnique(String, Object...)}, {@link #queryInt(String, Object...)})
 * which mirror the API surface of {@code AbstractDaoHibernate}.</p>
 *
 * @param <T> the entity type this DAO manages
 * @param <K> the primary key type
 */
public abstract class AbstractDaoJpa<T, K extends Serializable> implements OnmsDao<T, K> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDaoJpa.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final Class<T> entityClass;

    protected AbstractDaoJpa(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Returns the JPA EntityManager for subclass use.
     */
    protected EntityManager entityManager() {
        return entityManager;
    }

    // ---- OnmsDao implementation ----

    @Override
    public void lock() {
        // Acquire a pessimistic write lock on the access-lock row for this entity's table.
        // Uses a native query because the AccessLock entity lives in opennms-dao which
        // is not a dependency of daemon-common.
        String lockName = entityClass.getSimpleName().toUpperCase() + "_ACCESS";
        entityManager.createNativeQuery("SELECT lockName FROM accessLocks WHERE lockName = ?1 FOR UPDATE")
                .setParameter(1, lockName)
                .getSingleResult();
    }

    @Override
    public void initialize(Object obj) {
        org.hibernate.Hibernate.initialize(obj);
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public void clear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public int countAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(entityClass)));
        return entityManager.createQuery(cq).getSingleResult().intValue();
    }

    @Override
    public void delete(T entity) {
        entityManager.remove(
                entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    @Override
    public void delete(K key) {
        T entity = get(key);
        if (entity != null) {
            delete(entity);
        }
    }

    @Override
    public List<T> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        cq.from(entityClass);
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<T> findMatching(Criteria criteria) {
        // A full JpaCriteriaConverter that translates OpenNMS Criteria to JPA
        // CriteriaBuilder predicates is not yet implemented. Subclass DAOs should
        // use HQL via find()/findUnique() for now.
        throw new UnsupportedOperationException(
                "findMatching not yet implemented in AbstractDaoJpa — "
                + "subclass DAOs should use HQL via find()/findUnique() instead");
    }

    @Override
    public int countMatching(Criteria criteria) {
        throw new UnsupportedOperationException(
                "countMatching not yet implemented in AbstractDaoJpa — "
                + "subclass DAOs should use HQL via queryInt() instead");
    }

    @Override
    public T get(K id) {
        return entityManager.find(entityClass, id);
    }

    @Override
    public T load(K id) {
        return entityManager.getReference(entityClass, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public K save(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return (K) entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil().getIdentifier(entity);
    }

    @Override
    public void saveOrUpdate(T entity) {
        entityManager.merge(entity);
    }

    @Override
    public void update(T entity) {
        entityManager.merge(entity);
    }

    // ---- Helper methods (matching AbstractDaoHibernate API surface) ----

    /**
     * Execute an HQL query returning a list of entities.
     */
    @SuppressWarnings("unchecked")
    protected List<T> find(String hql) {
        return entityManager.createQuery(hql).getResultList();
    }

    /**
     * Execute an HQL query with positional parameters returning a list of entities.
     * Parameters are bound as {@code ?1}, {@code ?2}, etc.
     */
    @SuppressWarnings("unchecked")
    protected List<T> find(String hql, Object... values) {
        Query query = entityManager.createQuery(hql);
        for (int i = 0; i < values.length; i++) {
            query.setParameter(i + 1, values[i]);
        }
        return query.getResultList();
    }

    /**
     * Execute an HQL query expecting a single result, or {@code null} if none found.
     * Parameters are bound as {@code ?1}, {@code ?2}, etc.
     */
    protected T findUnique(String hql, Object... args) {
        TypedQuery<T> query = entityManager.createQuery(hql, entityClass);
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        List<T> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Execute an HQL query returning a list of arbitrary typed objects.
     * Parameters are bound as {@code ?1}, {@code ?2}, etc.
     */
    @SuppressWarnings("unchecked")
    protected <S> List<S> findObjects(Class<S> clazz, String hql, Object... values) {
        Query query = entityManager.createQuery(hql);
        for (int i = 0; i < values.length; i++) {
            query.setParameter(i + 1, values[i]);
        }
        return query.getResultList();
    }

    /**
     * Execute an HQL query returning a single integer result.
     */
    protected int queryInt(String hql) {
        Number result = (Number) entityManager.createQuery(hql).getSingleResult();
        return result.intValue();
    }

    /**
     * Execute an HQL query with positional parameters returning a single integer result.
     * Parameters are bound as {@code ?1}, {@code ?2}, etc.
     */
    protected int queryInt(String hql, Object... args) {
        Query query = entityManager.createQuery(hql);
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        Number result = (Number) query.getSingleResult();
        return result.intValue();
    }
}
