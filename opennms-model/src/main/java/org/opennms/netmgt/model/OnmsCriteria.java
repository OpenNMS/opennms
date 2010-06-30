/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Feb 11: Create and use our own extension of DetachedCriteria,
 *              OnmsDetachedCriteria, so we have access to the CriteriaImpl
 *              so we can use some methods on CriteriaImpl that are not
 *              currently available in DetachedCriteria (createAlias and
 *              createCriteria with the option to set the join type, in
 *              particular).  - dj@opennms.org 
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the/m
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.model;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.transform.ResultTransformer;

/**
 * <p>OnmsCriteria class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsCriteria {
    
    /** Constant <code>INNER_JOIN=Criteria.INNER_JOIN</code> */
    public static final int INNER_JOIN = Criteria.INNER_JOIN;
    /** Constant <code>LEFT_JOIN=Criteria.LEFT_JOIN</code> */
    public static final int LEFT_JOIN = Criteria.LEFT_JOIN;
    
    private OnmsDetachedCriteria m_criteria;
    private String m_entityName;
    private Integer m_firstResult = null;
    private Integer m_maxResults = null;
    
    /**
     * <p>Constructor for OnmsCriteria.</p>
     *
     * @param entityName a {@link java.lang.String} object.
     */
    public OnmsCriteria(String entityName) {
        this(entityName, OnmsDetachedCriteria.forEntityName(entityName));
    }
    
    /**
     * <p>Constructor for OnmsCriteria.</p>
     *
     * @param entityName a {@link java.lang.String} object.
     * @param alias a {@link java.lang.String} object.
     */
    public OnmsCriteria(String entityName, String alias) {
        this(entityName, OnmsDetachedCriteria.forEntityName(entityName, alias));
    }
    
    /**
     * <p>Constructor for OnmsCriteria.</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     */
    public OnmsCriteria(Class<?> clazz) {
        this(clazz.getName(), OnmsDetachedCriteria.forClass(clazz));
    }
    
    /**
     * <p>Constructor for OnmsCriteria.</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @param alias a {@link java.lang.String} object.
     */
    public OnmsCriteria(Class<?> clazz, String alias) {
        this(clazz.getName(), OnmsDetachedCriteria.forClass(clazz, alias));
    }
    
    /**
     * <p>Constructor for OnmsCriteria.</p>
     *
     * @param entityName a {@link java.lang.String} object.
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria.OnmsDetachedCriteria} object.
     */
    protected OnmsCriteria(String entityName, OnmsDetachedCriteria criteria) {
        m_entityName = entityName;
        m_criteria = criteria;
    }
    
    /**
     * <p>add</p>
     *
     * @param criterion a {@link org.hibernate.criterion.Criterion} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria add(Criterion criterion) {
        m_criteria.add(criterion);
        return this;
    }

    /**
     * <p>addOrder</p>
     *
     * @param order a {@link org.hibernate.criterion.Order} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria addOrder(Order order) {
        m_criteria.addOrder(order);
        return this;
    }

    /**
     * <p>createAlias</p>
     *
     * @param associationPath a {@link java.lang.String} object.
     * @param alias a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @throws org.hibernate.HibernateException if any.
     */
    public OnmsCriteria createAlias(String associationPath, String alias) throws HibernateException {
        m_criteria.createAlias(associationPath, alias);
        return this;
    }
    
    /**
     * <p>createAlias</p>
     *
     * @param associationPath a {@link java.lang.String} object.
     * @param alias a {@link java.lang.String} object.
     * @param joinType a int.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria createAlias(String associationPath, String alias, int joinType) {
        m_criteria.createAlias(associationPath, alias, joinType);
        return this;
    }

    /**
     * <p>createCriteria</p>
     *
     * @param associationPath a {@link java.lang.String} object.
     * @param alias a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria createCriteria(String associationPath, String alias) {
        return new OnmsCriteria(null, m_criteria.createCriteria(associationPath, alias));
    }

    /**
     * <p>createCriteria</p>
     *
     * @param associationPath a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria createCriteria(String associationPath) {
        return new OnmsCriteria(null,  m_criteria.createCriteria(associationPath) );
    }

    /**
     * <p>createCriteria</p>
     *
     * @param associationPath a {@link java.lang.String} object.
     * @param joinType a int.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria createCriteria(String associationPath, int joinType) {
        return new OnmsCriteria(null,  m_criteria.createCriteria(associationPath, joinType) );
    }

    /**
     * <p>getAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAlias() {
        return m_criteria.getAlias();
    }

    /**
     * <p>setFetchMode</p>
     *
     * @param associationPath a {@link java.lang.String} object.
     * @param mode a {@link org.hibernate.FetchMode} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @throws org.hibernate.HibernateException if any.
     */
    public OnmsCriteria setFetchMode(String associationPath, FetchMode mode) throws HibernateException {
        m_criteria.setFetchMode(associationPath, mode);
        return this;
    }

    /**
     * <p>setProjection</p>
     *
     * @param projection a {@link org.hibernate.criterion.Projection} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria setProjection(Projection projection) {
        m_criteria.setProjection(projection);
        return this;
    }

    /**
     * <p>setResultTransformer</p>
     *
     * @param resultTransformer a {@link org.hibernate.transform.ResultTransformer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public OnmsCriteria setResultTransformer(ResultTransformer resultTransformer) {
        m_criteria.setResultTransformer(resultTransformer);
        return this;
    }
    
    /**
     * <p>getDetachedCriteria</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsCriteria.OnmsDetachedCriteria} object.
     */
    public OnmsDetachedCriteria getDetachedCriteria() {
        return m_criteria;
    }

    /**
     * <p>resultsOfType</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @return a boolean.
     */
    public boolean resultsOfType(Class<?> clazz) {
        if (m_entityName == null) {
            return true;
        } else {
            return clazz.getName().endsWith(m_entityName);
        }
    }
    
    /**
     * <p>getFirstResult</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getFirstResult() {
    	return m_firstResult;
    }
    
    /**
     * <p>setFirstResult</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     */
    public void setFirstResult(Integer offset) {
    	m_firstResult = offset;
    }
    
    /**
     * <p>getMaxResults</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getMaxResults() {
    	return m_maxResults;
    }
    
    /**
     * <p>setMaxResults</p>
     *
     * @param limit a {@link java.lang.Integer} object.
     */
    public void setMaxResults(Integer limit) {
    	m_maxResults = limit;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "OnmsCriteria( " + m_criteria + ") limit " + m_maxResults + " offset " + m_firstResult;
    }



    /**
     * This is a subclass of Hibernate's DetachedCriteria, providing a few
     * more of the Criteria methods than are available in DetachedCriteria.
     * We create our own CriteriaImpl and pass it to the constructor for our
     * superclass, keeping a reference to the CriteriaImpl for ourselves so
     * we can use it for feature that DetachedCriteria doesn't support.
     * 
     * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
     */
    public static class OnmsDetachedCriteria extends DetachedCriteria {
        /**
         * 
         */
        private static final long serialVersionUID = -2016788794945601848L;
        private CriteriaImpl m_impl;

        protected OnmsDetachedCriteria(String entityName) {
            this(new CriteriaImpl(entityName, null));
        }
        
        protected OnmsDetachedCriteria(String entityName, String alias) {
            this(new CriteriaImpl(entityName, alias, null));
        }

        protected OnmsDetachedCriteria(CriteriaImpl impl) {
            super(impl, impl);
            m_impl = impl;
        }
        
        protected OnmsDetachedCriteria(CriteriaImpl impl, Criteria criteria) {
            super(impl, criteria);
            m_impl = impl;
        }
        
        public static OnmsDetachedCriteria forEntityName(String entityName) {
            return new OnmsDetachedCriteria(entityName);
        }
        
        public static OnmsDetachedCriteria forEntityName(String entityName, String alias) {
            return new OnmsDetachedCriteria(entityName, alias);
        }
        
        public static OnmsDetachedCriteria forClass(Class<?> clazz) {
            return new OnmsDetachedCriteria(clazz.getName());
        }
        
        public static OnmsDetachedCriteria forClass(Class<?> clazz, String alias) {
            return new OnmsDetachedCriteria(clazz.getName() , alias);
        }
        
        public OnmsDetachedCriteria createAlias(String associationPath, String alias, int joinType) {
            m_impl.createAlias(associationPath, alias, joinType);
            return this;
        }

        public OnmsDetachedCriteria createCriteria(String associationPath, int joinType) {
            return new OnmsDetachedCriteria(m_impl, m_impl.createCriteria(associationPath, joinType));
        }
        
        public OnmsDetachedCriteria createCriteria(String associationPath, String alias) {
            return new OnmsDetachedCriteria(m_impl, m_impl.createCriteria(associationPath));
        }

        public OnmsDetachedCriteria createCriteria(String associationPath, String alias, int joinType) {
            return new OnmsDetachedCriteria(m_impl, m_impl.createCriteria(associationPath, alias, joinType));
        }

        public OnmsDetachedCriteria createCriteria(String associationPath) throws HibernateException {
            return new OnmsDetachedCriteria(m_impl, m_impl.createCriteria(associationPath));
        }
    }
}
