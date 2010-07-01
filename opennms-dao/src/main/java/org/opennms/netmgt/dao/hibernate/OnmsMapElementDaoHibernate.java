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
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.OnmsMapElementDao;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;
import org.springframework.orm.hibernate3.HibernateCallback;


/**
 * <p>OnmsMapElementDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsMapElementDaoHibernate extends AbstractDaoHibernate<OnmsMapElement, Integer> implements OnmsMapElementDao {
    /**
     * <p>Constructor for OnmsMapElementDaoHibernate.</p>
     */
    public OnmsMapElementDaoHibernate() {
        super(OnmsMapElement.class);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public OnmsMapElement findElementById(int id) {
        return findUnique("from OnmsMapElement as element where element.id = ?", id);
    }

    /** {@inheritDoc} */
    public Collection<OnmsMapElement> findElementsByMapId(OnmsMap map) {
        return find("from OnmsMapElement as element where element.map = ?", map);
    }

    /** {@inheritDoc} */
    public Collection<OnmsMapElement> findElementsByNodeId(int nodeid) {
        Object[] values = {nodeid, OnmsMapElement.NODE_TYPE, OnmsMapElement.NODE_HIDE_TYPE};
        return find("from OnmsMapElement as element where element.elementId = ? and (element.type = ? or element.type = ? )", values);
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void deleteElementsByMapId(final OnmsMap map) {
        getHibernateTemplate().execute(
                                       new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                
             String hql = "delete from OnmsMapElement as element where element.map.id = :mapId";
             Query query = session.createQuery(hql);
             query.setInteger("mapId",map.getId());
             query.executeUpdate();
             return null;      
            } 
        });
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void deleteElementsByNodeid(final int nodeid) {
      getHibernateTemplate().execute(
                                     new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException, SQLException {
              
           String hql = "delete from OnmsMapElement as element where element.elementId = :nodeId and" +
           		" ( element.type = :typenode or element.type = :typemap )";
           Query query = session.createQuery(hql);
           query.setInteger("nodeId",nodeid);
           query.setString("typenode",OnmsMapElement.NODE_TYPE);
           query.setString("typemap",OnmsMapElement.NODE_HIDE_TYPE);
           query.executeUpdate();
           return null;      
          } 
      });
  }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void deleteElementsByType(final String type) {
        getHibernateTemplate().execute(
                                       new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                
             String hql = "delete from OnmsMapElement as element where element.type = :type";
             Query query = session.createQuery(hql);
             query.setString("type",type);
             query.executeUpdate();
             return null;      
            } 
        });

    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void deleteElementsByElementIdAndType(final int id,final String type) {
        getHibernateTemplate().execute(
                                       new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                
             String hql = "delete from OnmsMapElement as element where element.elementId = :id and element.type = :type";
             Query query = session.createQuery(hql);
             query.setInteger("id",id);
             query.setString("type",type);
             query.executeUpdate();
             return null;      
            } 
       });

      
    }

    /** {@inheritDoc} */
    public Collection<OnmsMapElement> findElementsByElementIdAndType(
            int elementId, String type) {
        Object[] values = {elementId, type};
        return  find("from OnmsMapElement as element where element.elementId = ? and element.type = ?", values);
    }

    /** {@inheritDoc} */
    public Collection<OnmsMapElement> findElementsByType(String type) {
        return find("from OnmsMapElement as element where element.type = ?", type);
    }

    /** {@inheritDoc} */
    public OnmsMapElement findElement(int elementId, String type, OnmsMap map) {
        Object[] values = {elementId, type, map};
        return  findUnique("from OnmsMapElement as element where element.elementId = ? and element.type = ? and element.map = ?", values);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void deleteElementsByMapType(final String mapType) {
        getHibernateTemplate().execute(
                                       new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                
             String hql = "delete from OnmsMapElement as element where element.id in ( select el.id from OnmsMapElement as el where el.map.type = ?)";
             Query query = session.createQuery(hql);
             query.setParameter(0, mapType);
             query.executeUpdate();
             return null;      
            }
        });
    }

    /** {@inheritDoc} */
    public Collection<OnmsMapElement> findElementsByMapIdAndType(int mapId,
            String type) {
        Object[] values = {mapId,type};
        return find("from OnmsMapElement as element where element.map.id = ? and element.type = ? ",values);
    }

    /** {@inheritDoc} */
    public Collection<OnmsMapElement> findMapElementsOnMap(int mapId) {
        Object[] values = {mapId,OnmsMapElement.MAP_TYPE,OnmsMapElement.MAP_HIDE_TYPE};
        return find("from OnmsMapElement as element where element.map.id = ? and (element.type = ? or element.type= ? )",values);
    }

    /** {@inheritDoc} */
    public Collection<OnmsMapElement> findNodeElementsOnMap(int mapId) {
        Object[] values = {mapId,OnmsMapElement.NODE_TYPE,OnmsMapElement.NODE_HIDE_TYPE};
        return find("from OnmsMapElement as element where element.map.id = ? and (element.type = ? or element.type= ? )",values);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public int countElementsOnMap(final int mapid) {
        Number nu = (Number) getHibernateTemplate().execute(
          new HibernateCallback() {
              public Object doInHibernate(Session session) throws HibernateException, SQLException {
                  
               String hql = "select count(*) from OnmsMapElement as element where element.map.id = ?)";
               Query query = session.createQuery(hql);
               query.setParameter(0, mapid);
               return query.uniqueResult();
              }
          });
        return nu.intValue();
  }
}
