//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.hibernate;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.OnmsDao;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public abstract class AbstractDaoHibernate<T, K extends Serializable> extends HibernateDaoSupport implements OnmsDao<T, K> {
	
	Class<T> m_entityClass;
	
	public AbstractDaoHibernate(Class<T> entityClass) {
		m_entityClass = entityClass;
	}
	

    public T initialize(T obj) {
        getHibernateTemplate().initialize(obj);
        return obj;
    }
    
    public void flush() {
        getHibernateTemplate().flush();
    }
    
    public void clear() {
        getHibernateTemplate().clear();
    }
    
    public void evict(T entity) {
        getHibernateTemplate().evict(entity);
    }
    
    public void merge(T entity) {
        getHibernateTemplate().merge(entity);
    }
    
    @SuppressWarnings("unchecked")
	public Collection<T> find(String query) {
        return getHibernateTemplate().find(query);
    }
    
    @SuppressWarnings("unchecked")
	public Collection<T> find(String query, Object... values) {
        return getHibernateTemplate().find(query, values);
    }
    
    protected int queryInt(final String query) {
        Object result = getHibernateTemplate().execute(new HibernateCallback() {
		    
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery(query)
                            .uniqueResult();
            }
            
        });
        return ((Integer)result).intValue();
    }

    protected T findUnique(final String query) {
        Object result = getHibernateTemplate().execute(new HibernateCallback() {
		    
		            public Object doInHibernate(Session session) throws HibernateException, SQLException {
		                return session.createQuery(query)
		                            .uniqueResult();
		            }
		            
		        });
		return m_entityClass.cast(result);
    }
    
    protected T findUnique(final String queryString, final Object... args) {
        Object result = getHibernateTemplate().execute(new HibernateCallback() {
		            
		            public Object doInHibernate(Session session) throws HibernateException, SQLException {
		            	Query query = session.createQuery(queryString);
		            	for (int i = 0; i < args.length; i++) {
							query.setParameter(i, args[i]);
						}
		                return query.uniqueResult();
		            }
		            
		        });
		return m_entityClass.cast(result);
    }
    
    public int countAll() {
    	return queryInt("select count(*) from "+m_entityClass.getName());
    }
    
	public void delete(T entity) {
		getHibernateTemplate().delete(entity);
	}

	@SuppressWarnings("unchecked")
	public Collection<T> findAll() {
		return getHibernateTemplate().loadAll(m_entityClass);
	}

	public T get(K id) {
		return m_entityClass.cast(getHibernateTemplate().get(m_entityClass, id));
	}

	public T load(K id) {
		return m_entityClass.cast(getHibernateTemplate().load(m_entityClass, id));
	}

	public void save(T entity) {
		getHibernateTemplate().save(entity);
	}

	public void saveOrUpdate(T entity) {
		getHibernateTemplate().saveOrUpdate(entity);
	}

	public void update(T entity) {
		getHibernateTemplate().update(entity);
	}

}
