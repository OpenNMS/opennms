/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * AbstractCachingDaoHibernate
 *
 * @author brozow
 * @version $Id: $
 */
public abstract class AbstractCachingDaoHibernate<T, DbKey extends Serializable, CacheKey> extends AbstractDaoHibernate<T, DbKey> {

    private final ThreadLocal<HashMap<CacheKey, T>> m_cache = new ThreadLocal<HashMap<CacheKey, T>>();
    private final boolean m_dbKeyMatchesCacheKey;

    /**
     * <p>Constructor for AbstractCachingDaoHibernate.</p>
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param dbKeyMatchesCacheKey a boolean.
     * @param <T> a T object.
     * @param <DbKey> a DbKey object.
     * @param <CacheKey> a CacheKey object.
     */
    public AbstractCachingDaoHibernate(Class<T> entityClass, boolean dbKeyMatchesCacheKey) {
        super(entityClass);
        m_dbKeyMatchesCacheKey = dbKeyMatchesCacheKey;
    }
    
    /**
     * <p>getKey</p>
     *
     * @param t a T object.
     * @return a CacheKey object.
     */
    abstract protected CacheKey getKey(T t);

    /** {@inheritDoc} */
    @Override
    public void clear() {
        m_cache.remove();
        super.clear();
    }
    
    /** {@inheritDoc} */
    @Override
    public void deleteAll(Collection<T> entities) throws DataAccessException {
        List<CacheKey> ids = Collections.emptyList();
        if (m_cache.get() != null) {
            ids = new ArrayList<CacheKey>(entities.size());
            for(T t : entities) {
                ids.add(getKey(t));
            }
        }
        
        super.deleteAll(entities);
        
        if (m_cache.get() != null) {
            for(CacheKey id : ids) {
                m_cache.get().remove(id);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void delete(T entity) throws DataAccessException {
        CacheKey id = getKey(entity);
        super.delete(entity);
        if (m_cache.get() != null) {
            m_cache.get().remove(id);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<T> findAll() throws DataAccessException {
        List<T> entities = super.findAll();
        
        HashMap<CacheKey, T> map = new HashMap<CacheKey, T>();
        for(T t : entities) {
            map.put(getKey(t), t);
        }
        m_cache.set(map);
        
        return entities;
    }

    /** {@inheritDoc} */
    @Override
    public T get(DbKey id) throws DataAccessException {
        
        if (m_cache.get() == null) {
            m_cache.set(new HashMap<CacheKey, T>());
        }
        
        if (m_dbKeyMatchesCacheKey) {
            T t = m_cache.get().get(id);
            if (t != null) {
                return t;
            }
        }

        T t = super.get(id);

        if (t != null) {
            m_cache.get().put(getKey(t), t);
        }
        
        return t;
        
    }

    /** {@inheritDoc} */
    @Override
    public T load(DbKey id) throws DataAccessException {
        if (m_cache.get() == null) {
            m_cache.set(new HashMap<CacheKey, T>());
        }
        
        if (m_dbKeyMatchesCacheKey) {
            T t = m_cache.get().get(id);
            if (t != null) {
                return t;
            }
        }

        T t = super.load(id);

        if (t != null) {
            m_cache.get().put(getKey(t), t);
        }
        
        return t;

    }

    /** {@inheritDoc} */
    @Override
    public void merge(T entity) {
        super.merge(entity);
        if (m_cache.get() != null) {
            m_cache.get().put(getKey(entity), entity);
        }
    }

    /** {@inheritDoc} */
    @Override
    public DbKey save(T entity) throws DataAccessException {
        DbKey retval = super.save(entity);
        if (m_cache.get() != null) {
            m_cache.get().put(getKey(entity), entity);
        }
        return retval;
    }

    /** {@inheritDoc} */
    @Override
    public void saveOrUpdate(T entity) throws DataAccessException {
        super.saveOrUpdate(entity);
        if (m_cache.get() != null) {
            m_cache.get().put(getKey(entity), entity);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void update(T entity) throws DataAccessException {
        super.update(entity);
        if (m_cache.get() != null) {
            m_cache.get().put(getKey(entity), entity);
        }
    }
    
    /**
     * <p>findByCacheKey</p>
     *
     * @param queryString a {@link java.lang.String} object.
     * @param key a CacheKey object.
     * @return a T object.
     */
    protected T findByCacheKey(String queryString, CacheKey key) {
        T t = null;
        if (m_cache.get() != null) {
            t = m_cache.get().get(key);
            if (t != null) {
                return t;
            }
        }
        
        t = findUnique(queryString, key);
        
        if (t != null) {
            if (m_cache.get() == null) {
                m_cache.set(new HashMap<CacheKey, T>());
            }
            m_cache.get().put(key, t);
        }
        
        return t;
    }
    
}
