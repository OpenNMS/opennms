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
        } {
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
