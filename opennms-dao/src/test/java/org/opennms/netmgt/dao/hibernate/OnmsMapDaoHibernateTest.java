package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.AbstractTransactionalDaoTestCase;
import org.opennms.netmgt.model.OnmsMap;

import java.util.Collection;

public class OnmsMapDaoHibernateTest  extends AbstractTransactionalDaoTestCase {
    public void testInitialize() {
        // do nothing, just test that setUp() / tearDown() works
    }

    public void testSaveOnmsMap() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap", "admin");
        getOnmsMapDao().save(map);
        getOnmsMapDao().flush();
    	getOnmsMapDao().clear();

        // Now pull it back up and make sure it saved.
        Object [] args = { map.getId() };
        assertEquals(1, getJdbcTemplate().queryForInt("select count(*) from map where mapId = ?", args));

        OnmsMap map2 = getOnmsMapDao().findMapById(map.getId());
    	assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
    }

    public void testFindById() {
        OnmsMap map = getOnmsMapDao().findMapById(1);
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

    public void testFindMapsByName() {
        Collection<OnmsMap> maps = getOnmsMapDao().findMapsByName("DB_Pop_Test_Map");

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

    public void testFindMapsLike() {
        Collection<OnmsMap> maps = getOnmsMapDao().findMapsLike("Pop_Test");

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

    public void testDeleteOnmsMap() {
        OnmsMap map = getOnmsMapDao().findMapById(1);
        getOnmsMapDao().delete(map);

        assertNull(getOnmsMapDao().findMapById(1));
    }
}
