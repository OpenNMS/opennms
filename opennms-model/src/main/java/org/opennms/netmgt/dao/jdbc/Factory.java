/**
 * 
 */
package org.opennms.netmgt.dao.jdbc;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;


abstract public class Factory implements InitializingBean {
    
    private DataSource m_dataSource;
    private Class m_clazz;
    
    protected Factory(Class clazz) {
        m_clazz = clazz;
    }
    
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }
    
    public DataSource getDataSource() {
        return m_dataSource;
    }
    
	public Object get(Class clazz, Object id) {
		Object cached = Cache.retrieve(clazz, id);
		if (cached == null) {
			cached = createWithId(id);
			Cache.store(clazz, id, cached);
		}
		return cached;
		
	}

	private Object createWithId(Object id) {
		Object obj = create();
		assignId(obj, id);
		return obj;
	}
    
    public void afterPropertiesSet() {
        if (m_dataSource == null)
            throw new IllegalStateException(ClassUtils.getShortName(getClass())+" must be initialized with a DataSource");
        
        Cache.registerFactory(m_clazz, this);
    }


	abstract protected void assignId(Object obj, Object id);
	abstract protected Object create();

	
}