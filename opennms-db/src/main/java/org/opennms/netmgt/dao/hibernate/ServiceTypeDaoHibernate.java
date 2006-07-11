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

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;
import org.springframework.orm.hibernate3.HibernateCallback;

public class ServiceTypeDaoHibernate extends AbstractDaoHibernate implements ServiceTypeDao {

    public OnmsServiceType load(Integer id) {
        return (OnmsServiceType)getHibernateTemplate().load(OnmsServiceType.class, id);
    }

    public OnmsServiceType get(Integer id) {
        return (OnmsServiceType)getHibernateTemplate().get(OnmsServiceType.class, id);
    }

    public OnmsServiceType findByName(final String name) {
        return (OnmsServiceType)getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createQuery("from OnmsServiceType as svcType where svcType.name = ?")
                            .setString(0, name)
                            .setCacheable(true)
                            .uniqueResult();
            }
            
        });
    }

    public void save(OnmsServiceType serviceType) {
        getHibernateTemplate().save(serviceType);
    }

    public void update(OnmsServiceType serviceType) {
        getHibernateTemplate().update(serviceType);
    }

    public Collection findAll() {
        return getHibernateTemplate().loadAll(OnmsServiceType.class);
    }

    public int countAll() {
        return ((Integer)findUnique("select count(*) from OnmsServiceType")).intValue();
    }

}
