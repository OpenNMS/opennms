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

import org.opennms.netmgt.model.OnmsMapElement;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.dao.OnmsMapElementDao;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Session;
import org.hibernate.HibernateException;

import java.util.Collection;
import java.sql.SQLException;

public class OnmsMapElementDaoHibernate extends AbstractDaoHibernate<OnmsMapElement, Integer> implements OnmsMapElementDao {
    public OnmsMapElementDaoHibernate() {
        super(OnmsMapElement.class);
    }

    @SuppressWarnings("unchecked")
    public Collection<OnmsMapElement> findAll(final Integer offset, final Integer limit) {
        return (Collection<OnmsMapElement>)getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsMap.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }

        });
    }

    public OnmsMapElement findMapElementById(int id) {
        return findUnique("from OnmsMapElement as element where element.id = ?", id);
    }

    public Collection<OnmsMapElement> findMapElementsByMapId(int id) {
        return find("from OnmsMapElement as element where element.mapId = ?", id);
    }

    public void deleteElementsByMapId(int mapId) {
        for(OnmsMapElement elem :  find("from OnmsMapElement as element where element.mapId = ?", mapId)) {
            delete(elem);
        }
    }

    public void deleteElementsByType(String type) {
        for(OnmsMapElement elem :  find("from OnmsMapElement as element where element.type = ?", type)) {
            delete(elem);
        }
    }

    public void deleteElementsByIdandType(int id, String type) {
        Object[] values = {id, type};
        for(OnmsMapElement elem :  find("from OnmsMapElement as element where element.elementId = ? and element.type = ?", values)) {
            delete(elem);
        }
        
    }
}
