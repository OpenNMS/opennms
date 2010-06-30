package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.model.OnmsMap;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.HibernateException;

import java.util.Collection;
import java.util.Date;
import java.sql.SQLException;

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
    public Collection<OnmsMap> findAll(final Integer offset, final Integer limit) {
        return (Collection<OnmsMap>)getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsMap.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }

        });
    }

    /** {@inheritDoc} */
    public Collection<OnmsMap> findMapsLike(String mapLabel) {
        return find("from OnmsMap as map where map.name like ?", "%" + mapLabel + "%");
    }

    /** {@inheritDoc} */
    public Collection<OnmsMap> findMapsByName(String mapLabel) {
        return find("from OnmsMap as map where map.name = ?", mapLabel);
    }

    /** {@inheritDoc} */
    public OnmsMap findMapById(int id) {
        return findUnique("from OnmsMap as map where map.id = ?", id);
    }

    /** {@inheritDoc} */
    public Collection<OnmsMap> findMapsByNameAndType(String mapName, String mapType) {
        Object[] values = {mapName, mapType};
        return find("from OnmsMap as map where map.name = ? and map.type = ?", values);
    }

    /** {@inheritDoc} */
    public Collection<OnmsMap> findMapsByType(String mapType) {
        return find("from OnmsMap as map where map.type = ?", mapType);
    }

    /**
     * <p>findAutoMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsMap> findAutoMaps() {
        return findMapsByType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
    }

    /**
     * <p>findUserMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsMap> findUserMaps() {
        return findMapsByType(OnmsMap.USER_GENERATED_MAP);
    }

    /**
     * <p>findSaveMaps</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<OnmsMap> findSaveMaps() {
        return findMapsByType(OnmsMap.AUTOMATIC_SAVED_MAP);    
    }

    /** {@inheritDoc} */
    public Collection<OnmsMap> findMapsByGroup(String group) {
        return find("from OnmsMap as map where map.mapGroup = ?", group);
    }

    /** {@inheritDoc} */
    public Collection<OnmsMap> findMapsByOwner(String owner) {
        return find("from OnmsMap as map where map.owner = ?", owner);
    }

    /** {@inheritDoc} */
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
    public Collection<OnmsMap> findAutoAndSaveMaps() {
        Object[] values = {OnmsMap.AUTOMATIC_SAVED_MAP, OnmsMap.AUTOMATICALLY_GENERATED_MAP};
        return find("from OnmsMap as map where map.type = ? or map.type = ? ", values);
    }
    
    /** {@inheritDoc} */
    public int updateAllAutomatedMap(final Date time) {
        return getHibernateTemplate().execute(
                                       new HibernateCallback<Integer>() {
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
