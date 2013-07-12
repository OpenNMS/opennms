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

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.OnmsMapDao;
import org.opennms.netmgt.model.OnmsMap;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * <p>OnmsMapDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsMapDaoHibernate extends AbstractDaoHibernate<OnmsMap, Integer> implements OnmsMapDao {
    /**
     * <p>Constructor for OnmsMapDaoHibernate.</p>
     */
    public OnmsMapDaoHibernate() {
        super(OnmsMap.class);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<OnmsMap> findAll(final Integer offset, final Integer limit) {
        return getHibernateTemplate().execute(new HibernateCallback<Collection<OnmsMap>>() {

            @Override
            public Collection<OnmsMap> doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsMap.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }

        });
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMap> findMapsLike(String mapLabel) {
        return find("from OnmsMap as map where map.name like ?", "%" + mapLabel + "%");
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMap> findMapsByName(String mapLabel) {
        return find("from OnmsMap as map where map.name = ?", mapLabel);
    }

    /** {@inheritDoc} */
    @Override
    public OnmsMap findMapById(int id) {
        return findUnique("from OnmsMap as map where map.id = ?", id);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMap> findMapsByNameAndType(String mapName, String mapType) {
        Object[] values = {mapName, mapType};
        return find("from OnmsMap as map where map.name = ? and map.type = ?", values);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMap> findMapsByType(String mapType) {
        return find("from OnmsMap as map where map.type = ?", mapType);
    }

    /**
     * <p>findAutoMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsMap> findAutoMaps() {
        return findMapsByType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
    }

    /**
     * <p>findUserMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsMap> findUserMaps() {
        return findMapsByType(OnmsMap.USER_GENERATED_MAP);
    }

    /**
     * <p>findSaveMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsMap> findSaveMaps() {
        return findMapsByType(OnmsMap.AUTOMATIC_SAVED_MAP);    
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMap> findMapsByGroup(String group) {
        return find("from OnmsMap as map where map.mapGroup = ?", group);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMap> findMapsByOwner(String owner) {
        return find("from OnmsMap as map where map.owner = ?", owner);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsMap> findVisibleMapsByGroup(String group) {
        Object[] values = {OnmsMap.ACCESS_MODE_ADMIN, OnmsMap.ACCESS_MODE_USER,OnmsMap.ACCESS_MODE_GROUP,group};
        return find("from OnmsMap as map where map.accessMode = ? or map.accessMode = ? or " +
        		"(map.accessMode = ? and map.mapGroup = ?)", values);
    }

    /**
     * <p>findAutoAndSaveMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsMap> findAutoAndSaveMaps() {
        Object[] values = {OnmsMap.AUTOMATIC_SAVED_MAP, OnmsMap.AUTOMATICALLY_GENERATED_MAP};
        return find("from OnmsMap as map where map.type = ? or map.type = ? ", values);
    }
    
    /** {@inheritDoc} */
    @Override
    public int updateAllAutomatedMap(final Date time) {
        return getHibernateTemplate().execute(
                                       new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException, SQLException {
                
             String hql = "update OnmsMap as map set map.lastModifiedTime = :time where map.type = :type";
             Query query = session.createQuery(hql);
             query.setTimestamp("time", time);
             query.setString("type", OnmsMap.AUTOMATICALLY_GENERATED_MAP);
             return query.executeUpdate();
            } 
        });

    }
}
