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

    public void testSaveOnmsMap2() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap2", "admin",969,726);
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
        assertEquals(map.getAccessMode().trim(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());

    }

    public void testSaveOnmsMap3() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap3", "admin",OnmsMap.ACCESS_MODE_GROUP, 969,726);
        getOnmsMapDao().save(map);
        getOnmsMapDao().flush();
        getOnmsMapDao().clear();

        // Now pull it back up and make sure it saved.
        Object [] args = { map.getId() };
        assertEquals(1, getJdbcTemplate().queryForInt("select count(*) from map where mapId = ?", args));

        OnmsMap map2 = getOnmsMapDao().findMapById(map.getId());
        assertNotSame(map, map2);
        assertEquals(map.getName(), map2.getName());
        assertEquals(map.getOwner(), map2.getOwner().trim());
        assertEquals(map.getAccessMode(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());
    }

    public void testSaveOnmsMap4() {
        // Create a new map and save it.
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestMap4", "users","11aabb","admin",OnmsMap.ACCESS_MODE_GROUP, OnmsMap.USER_GENERATED_MAP,800,600);
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
        assertEquals(map.getAccessMode(), map2.getAccessMode().trim());
        assertEquals(map.getUserLastModifies(), map2.getUserLastModifies());
        assertEquals(map.getLastModifiedTime(), map2.getLastModifiedTime());
        assertEquals(map.getCreateTime(), map2.getCreateTime());
        assertEquals(map.getWidth(), map2.getWidth());
        assertEquals(map.getHeight(), map2.getHeight());
    }


    public void testFindById() {
        OnmsMap map = getOnmsMapDao().findMapById(58);
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

    public void testFindMapsByNameAndTypeOk() {
        Collection<OnmsMap> maps = getOnmsMapDao().findMapsByNameAndType("DB_Pop_Test_Map",OnmsMap.USER_GENERATED_MAP);

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }

    public void testFindMapsByNameAndTypeKo() {
        Collection<OnmsMap> maps = getOnmsMapDao().findMapsByNameAndType("DB_Pop_Test_Map",OnmsMap.AUTOMATICALLY_GENERATED_MAP);

        assertEquals(0, maps.size());
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

    public void testIsNew() {
        Collection<OnmsMap> maps = getOnmsMapDao().findMapsLike("Pop_Test");

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals(false, map.isNew());
    }

    public void testIsNew2() {
        Collection<OnmsMap> maps = getOnmsMapDao().findMapsByNameAndType("DB_Pop_Test_Map",OnmsMap.USER_GENERATED_MAP);

        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals(false, map.isNew());
    }

    public void testFindMapsByType() {
        Collection<OnmsMap> maps = getOnmsMapDao().findMapsByType("X");
        assertEquals(0, maps.size());
    }

    public void testFindAutoMaps() {
        Collection<OnmsMap> maps = getOnmsMapDao().findAutoMaps();
        assertEquals(0, maps.size());
    }

    public void testFindUserMaps() {
        Collection<OnmsMap> maps = getOnmsMapDao().findUserMaps();
        assertEquals(1, maps.size());
    }

    public void testDeleteOnmsMap() {
        OnmsMap map = getOnmsMapDao().findMapById(58);
        getOnmsMapDao().delete(map);

        assertNull(getOnmsMapDao().findMapById(58));
    }
    
    public void testFindMapByOwner() {
        Collection<OnmsMap> maps = getOnmsMapDao().findMapsByOwner("admin");
        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());
    }
    
    public void testFindMapbyGroup() {
        Collection<OnmsMap> maps = getOnmsMapDao().findMapsByGroup("admin");
        assertEquals(1, maps.size());
        OnmsMap map = maps.iterator().next();
        assertEquals("DB_Pop_Test_Map", map.getName());
        assertEquals("fake_background.jpg", map.getBackground());
        assertEquals(OnmsMap.ACCESS_MODE_ADMIN, map.getAccessMode());
        assertEquals(OnmsMap.USER_GENERATED_MAP, map.getType());        
    }

    public void testFindMapbyGroup1() {
        Collection<OnmsMap> maps = getOnmsMapDao().findMapsByGroup("");
        assertEquals(0, maps.size());
    }

    
    
    public void testFindVisibleMapByGroup() {
        // create a new map
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestVisibleMap", "admin",OnmsMap.ACCESS_MODE_GROUP, 969,726);
        map.setMapGroup("testGroup");
        getOnmsMapDao().save(map);
        getOnmsMapDao().flush();
        getOnmsMapDao().clear();
        Collection<OnmsMap> maps = getOnmsMapDao().findVisibleMapsByGroup("testGroup");
        assertEquals(2, maps.size());
    }

    public void testFindVisibleMapByGroup2() {
        // create a new map
        OnmsMap map = new OnmsMap("onmsMapDaoHibernateTestVisibleMap", "admin",OnmsMap.ACCESS_MODE_GROUP, 969,726);
        map.setMapGroup("testGroup");
        getOnmsMapDao().save(map);
        getOnmsMapDao().flush();
        getOnmsMapDao().clear();
        Collection<OnmsMap> maps = getOnmsMapDao().findVisibleMapsByGroup("wrongGroup");
        assertEquals(1, maps.size());
    }

    
}
