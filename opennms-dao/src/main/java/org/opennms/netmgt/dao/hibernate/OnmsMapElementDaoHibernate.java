/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

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

    public Collection<OnmsMapElement> findMapElementsByMapId(OnmsMap map) {
        return find("from OnmsMapElement as element where element.map = ?", map);
    }

    public void deleteElementsByMapId(OnmsMap map) {
        for(OnmsMapElement elem :  find("from OnmsMapElement as element where element.map = ?", map)) {
            delete(elem);
        }
    }

    public void deleteElementsByType(String type) {
        for(OnmsMapElement elem :  find("from OnmsMapElement as element where element.type = ?", type)) {
            delete(elem);
        }
    }

    public void deleteElementsByElementIdAndType(int id, String type) {
        Object[] values = {id, type};
        for(OnmsMapElement elem :  find("from OnmsMapElement as element where element.elementId = ? and element.type = ?", values)) {
            delete(elem);
        }
        
    }

    public Collection<OnmsMapElement> findElementsByElementIdAndType(
            int elementId, String type) {
        Object[] values = {elementId, type};
        return  find("from OnmsMapElement as element where element.elementId = ? and element.type = ?", values);
    }

    public Collection<OnmsMapElement> findElementsByType(String type) {
        return find("from OnmsMapElement as element where element.type = ?", type);
    }

    public OnmsMapElement findMapElement(int elementId, String type, OnmsMap map) {
        Object[] values = {elementId, type, map};
        return  findUnique("from OnmsMapElement as element where element.elementId = ? and element.type = ? and element.map = ?", values);
    }
}
