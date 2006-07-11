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