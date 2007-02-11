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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.model;

import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.transform.ResultTransformer;

public class OnmsCriteria {
    
    private DetachedCriteria m_criteria;
    private String m_entityName;
    private Integer m_firstResult = null;
    private Integer m_maxResults = null;
    
    public OnmsCriteria(String entityName) {
        this(entityName, DetachedCriteria.forEntityName(entityName));
    }
    
    public OnmsCriteria(String entityName, String alias) {
        this(entityName, DetachedCriteria.forEntityName(entityName, alias));
    }
    
    public OnmsCriteria(Class clazz) {
        this(clazz.getName(), DetachedCriteria.forClass(clazz));
    }
    
    public OnmsCriteria(Class clazz, String alias) {
        this(clazz.getName(), DetachedCriteria.forClass(clazz, alias));
    }
    
    protected OnmsCriteria(String entityName, DetachedCriteria criteria) {
        m_entityName = entityName;
        m_criteria = criteria;
    }
    
    public OnmsCriteria add(Criterion criterion) {
        m_criteria.add(criterion);
        return this;
    }

    public OnmsCriteria addOrder(Order order) {
        m_criteria.addOrder(order);
        return this;
    }

    public OnmsCriteria createAlias(String associationPath, String alias)
    throws HibernateException {
        m_criteria.createAlias(associationPath, alias);
        return this;
    }

    public OnmsCriteria createCriteria(String associationPath, String alias) {
        return new OnmsCriteria(null, m_criteria.createCriteria(associationPath, alias));
    }

    public OnmsCriteria createCriteria(String associationPath) {
        return new OnmsCriteria(null,  m_criteria.createCriteria(associationPath) );
    }

    public String getAlias() {
        return m_criteria.getAlias();
    }

    public OnmsCriteria setFetchMode(String associationPath, FetchMode mode)
    throws HibernateException {
        m_criteria.setFetchMode(associationPath, mode);
        return this;
    }

    public OnmsCriteria setProjection(Projection projection) {
        m_criteria.setProjection(projection);
        return this;
    }

    public OnmsCriteria setResultTransformer(ResultTransformer resultTransformer) {
        m_criteria.setResultTransformer(resultTransformer);
        return this;
    }
    
    public DetachedCriteria getDetachedCriteria() {
        return m_criteria;
    }

    public boolean resultsOfType(Class clazz) {
        if (m_entityName == null) {
            return true;
        } else {
            return clazz.getName().endsWith(m_entityName);
        }
    }
    
    public Integer getFirstResult() {
    	return m_firstResult;
    }
    
    public void setFirstResult(Integer offset) {
    	m_firstResult = offset;
    }
    
    public Integer getMaxResults() {
    	return m_maxResults;
    }
    
    public void setMaxResults(Integer limit) {
    	m_maxResults = limit;
    }

}
