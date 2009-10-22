package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.OnmsMapDao;
import org.opennms.netmgt.model.OnmsMap;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Session;
import org.hibernate.HibernateException;

import java.util.Collection;
import java.sql.SQLException;

public class OnmsMapDaoHibernate extends AbstractDaoHibernate<OnmsMap, Integer> implements OnmsMapDao {
    public OnmsMapDaoHibernate() {
        super(OnmsMap.class);
    }

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

    public Collection<OnmsMap> findMapsLike(String mapLabel) {
        return find("from OnmsMap as map where map.name like ?", "%" + mapLabel + "%");
    }

    public Collection<OnmsMap> findMapsByName(String mapLabel) {
        return find("from OnmsMap as map where map.name = ?", mapLabel);
    }

    public OnmsMap findMapById(int id) {
        return findUnique("from OnmsMap as map where map.id = ?", id);
    }

    public Collection<OnmsMap> findMapsByNameAndType(String mapName, String mapType) {
        Object[] values = {mapName, mapType};
        return find("from OnmsMap as map where map.name = ? and map.type = ?", values);
    }

    public Collection<OnmsMap> findMapsByType(String mapType) {
        return find("from OnmsMap as map where map.type = ?", mapType);
    }

    public Collection<OnmsMap> findAutoMaps() {
        return findMapsByType(OnmsMap.AUTOMATICALLY_GENERATED_MAP);
    }

    public Collection<OnmsMap> findUserMaps() {
        return findMapsByType(OnmsMap.USER_GENERATED_MAP);
    }

    public Collection<OnmsMap> findSaveMaps() {
        return findMapsByType(OnmsMap.AUTOMATIC_SAVED_MAP);    
    }

    public Collection<OnmsMap> findMapsByGroup(String group) {
        return find("from OnmsMap as map where map.mapGroup = ?", group);
    }

    public Collection<OnmsMap> findMapsByOwner(String owner) {
        return find("from OnmsMap as map where map.owner = ?", owner);
    }

    public Collection<OnmsMap> findVisibleMapsByGroup(String group) {
        Object[] values = {OnmsMap.ACCESS_MODE_ADMIN, OnmsMap.ACCESS_MODE_USER,OnmsMap.ACCESS_MODE_GROUP,group};
        return find("from OnmsMap as map where map.accessMode = ? or map.accessMode = ? or " +
        		"(map.accessMode = ? and map.mapGroup = ?)", values);
    }
    
    
}
