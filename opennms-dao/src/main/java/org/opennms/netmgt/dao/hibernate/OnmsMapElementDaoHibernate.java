/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;

import org.hibernate.Query;
import org.opennms.netmgt.dao.api.OnmsMapElementDao;
import org.opennms.netmgt.model.OnmsMap;
import org.opennms.netmgt.model.OnmsMapElement;

public class OnmsMapElementDaoHibernate extends AbstractDaoHibernate<OnmsMapElement, Integer> implements OnmsMapElementDao {
    /**
     * <p>Constructor for OnmsMapElementDaoHibernate.</p>
     */
    public OnmsMapElementDaoHibernate() {
        super(OnmsMapElement.class);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<OnmsMapElement> findAll(final Integer offset, final Integer limit) {
        return sessionFactory.getCurrentSession().createCriteria(OnmsMap.class)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .list();
    }

    /** {@inheritDoc} */
    @Override
    public OnmsMapElement findElementById(int id) {
        return findUnique("from OnmsMapElement as element where element.id = ?", id);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMapElement> findElementsByMapId(OnmsMap map) {
        return find("from OnmsMapElement as element where element.map = ?", map);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMapElement> findElementsByNodeId(int nodeid) {
        Object[] values = {nodeid, OnmsMapElement.NODE_TYPE, OnmsMapElement.NODE_HIDE_TYPE};
        return find("from OnmsMapElement as element where element.elementId = ? and (element.type = ? or element.type = ? )", values);
    }
    
    /** {@inheritDoc} */
    @Override
    public void deleteElementsByMapId(final OnmsMap map) {
         String hql = "delete from OnmsMapElement as element where element.map.id = :mapId";
         Query query = sessionFactory.getCurrentSession().createQuery(hql);
         query.setInteger("mapId",map.getId());
         query.executeUpdate();
    }

    /** {@inheritDoc} */
    @Override
    public void deleteElementsByNodeid(final int nodeid) {
        String hql = "delete from OnmsMapElement as element where element.elementId = :nodeId and" +
        " ( element.type = :typenode or element.type = :typemap )";
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setInteger("nodeId",nodeid);
        query.setString("typenode",OnmsMapElement.NODE_TYPE);
        query.setString("typemap",OnmsMapElement.NODE_HIDE_TYPE);
        query.executeUpdate();
    }

    /** {@inheritDoc} */
    @Override
    public void deleteElementsByType(final String type) {
         String hql = "delete from OnmsMapElement as element where element.type = :type";
         Query query = sessionFactory.getCurrentSession().createQuery(hql);
         query.setString("type",type);
         query.executeUpdate();
    }

    /** {@inheritDoc} */
    @Override
    public void deleteElementsByElementIdAndType(final int id,final String type) {
         String hql = "delete from OnmsMapElement as element where element.elementId = :id and element.type = :type";
         Query query = sessionFactory.getCurrentSession().createQuery(hql);
         query.setInteger("id",id);
         query.setString("type",type);
         query.executeUpdate();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMapElement> findElementsByElementIdAndType(
            int elementId, String type) {
        Object[] values = {elementId, type};
        return  find("from OnmsMapElement as element where element.elementId = ? and element.type = ?", values);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMapElement> findElementsByType(String type) {
        return find("from OnmsMapElement as element where element.type = ?", type);
    }

    /** {@inheritDoc} */
    @Override
    public OnmsMapElement findElement(int elementId, String type, OnmsMap map) {
        Object[] values = {elementId, type, map};
        return  findUnique("from OnmsMapElement as element where element.elementId = ? and element.type = ? and element.map = ?", values);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteElementsByMapType(final String mapType) {
         String hql = "delete from OnmsMapElement as element where element.id in ( select el.id from OnmsMapElement as el where el.map.type = ?)";
         Query query = sessionFactory.getCurrentSession().createQuery(hql);
         query.setParameter(0, mapType);
         query.executeUpdate();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMapElement> findElementsByMapIdAndType(int mapId,
            String type) {
        Object[] values = {mapId,type};
        return find("from OnmsMapElement as element where element.map.id = ? and element.type = ? ",values);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMapElement> findMapElementsOnMap(int mapId) {
        Object[] values = {mapId,OnmsMapElement.MAP_TYPE,OnmsMapElement.MAP_HIDE_TYPE};
        return find("from OnmsMapElement as element where element.map.id = ? and (element.type = ? or element.type= ? )",values);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMapElement> findNodeElementsOnMap(int mapId) {
        Object[] values = {mapId,OnmsMapElement.NODE_TYPE,OnmsMapElement.NODE_HIDE_TYPE};
        return find("from OnmsMapElement as element where element.map.id = ? and (element.type = ? or element.type= ? )",values);
    }

    /** {@inheritDoc} */
    @Override
    public int countElementsOnMap(final int mapid) {
        String hql = "select count(*) from OnmsMapElement as element where element.map.id = ?)";
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter(0, mapid);
        return ((Number) query.uniqueResult()).intValue();
    }
}
