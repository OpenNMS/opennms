package org.opennms.netmgt.dao;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SnmpCollectionMibGroupDaoIT {

    @Autowired
    private SnmpCollectionMibGroupDao mibGroupDao;

    @Autowired
    private SnmpCollectionSourceDao snmpSourceDao;

    @Autowired
    private SessionFactory sessionFactory;

    private SnmpCollectionSource source;
    private SnmpCollectionMibGroup mibGroup;

    @Before
    @Transactional
    public void setUp() {
        mibGroupDao.deleteAll(mibGroupDao.findAll());
        mibGroupDao.flush();
        snmpSourceDao.deleteAll(snmpSourceDao.findAll());
        snmpSourceDao.flush();

        source = new SnmpCollectionSource();
        source.setName("JUnit Source");
        source.setEnabled(true);
        source.setDescription("JUnit Description");
        snmpSourceDao.saveOrUpdate(source);
        snmpSourceDao.flush();

        mibGroup = new SnmpCollectionMibGroup();
        mibGroup.setName("Mib-Group-1");
        mibGroup.setEnabled(true);
        mibGroup.setIfType("ethernetCsmacd");
        mibGroup.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        mibGroup.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        mibGroup.setMibObjProperties("{\"property\":\"value\"}");
        mibGroup.setCollectionSource(source);
        mibGroupDao.saveOrUpdate(mibGroup);
        mibGroupDao.flush();
    }

    @After
    @Transactional
    public void tearDown() {
        mibGroupDao.deleteAll(mibGroupDao.findAll());
        mibGroupDao.flush();
        snmpSourceDao.deleteAll(snmpSourceDao.findAll());
        snmpSourceDao.flush();
    }

    @Test
    @Transactional
    public void testFindByNameAndSource() {
        SnmpCollectionMibGroup found = mibGroupDao.findByNameAndSource("Mib-Group-1", source.getId());
        assertNotNull(found);
        assertEquals("Mib-Group-1", found.getName());
        assertEquals(source.getId(), found.getCollectionSource().getId());
        assertEquals("ethernetCsmacd", found.getIfType());
        assertEquals("IF-MIB::ifEntry,IF-MIB::ifXEntry", found.getMibGroupNames());
        assertEquals("ifIndex,ifDescr,ifOperStatus", found.getMibObjects());
        assertEquals("{\"property\":\"value\"}", found.getMibObjProperties());
        assertTrue(found.getEnabled());
    }

    @Test
    @Transactional
    public void testFindByNameAndSourceReturnsNullIfNotExist() {
        SnmpCollectionMibGroup found = mibGroupDao.findByNameAndSource("Nonexistent", source.getId());
        assertNull(found);
    }

    @Test
    @Transactional
    public void testGetById() {
        SnmpCollectionMibGroup found = mibGroupDao.get(mibGroup.getId());
        assertNotNull(found);
        assertEquals(mibGroup.getName(), found.getName());
        assertEquals(mibGroup.getMibObjects(), found.getMibObjects());
    }

    @Test
    @Transactional
    public void testFindAllEnabled() {
        List<SnmpCollectionMibGroup> enabledList = mibGroupDao.findAllEnabled();
        assertFalse(enabledList.isEmpty());
        assertTrue(enabledList.stream().anyMatch(mg -> "Mib-Group-1".equals(mg.getName())));
        assertTrue(enabledList.stream().allMatch(SnmpCollectionMibGroup::getEnabled));
    }

    @Test
    @Transactional
    public void testFindAllBySource() {
        List<SnmpCollectionMibGroup> bySourceList = mibGroupDao.findAllBySource(source.getId());
        assertNotNull(bySourceList);
        assertFalse(bySourceList.isEmpty());
        assertTrue(bySourceList.stream().allMatch(mg -> mg.getCollectionSource().getId().equals(source.getId())));
    }

    @Test
    @Transactional
    public void testFindAllEnabledOnlyReturnsEnabled() {
        // Add a disabled group
        SnmpCollectionMibGroup disabled = new SnmpCollectionMibGroup();
        disabled.setName("DisabledGroup");
        disabled.setEnabled(false);
        disabled.setIfType("loopback");
        disabled.setMibGroupNames("IF-MIB::ifLoopback");
        disabled.setMibObjects("ifIndex,ifOperStatus");
        disabled.setMibObjProperties("{}");
        disabled.setCollectionSource(source);
        mibGroupDao.saveOrUpdate(disabled);
        mibGroupDao.flush();

        List<SnmpCollectionMibGroup> enabledList = mibGroupDao.findAllEnabled();
        assertTrue(enabledList.stream().allMatch(SnmpCollectionMibGroup::getEnabled));
        assertFalse(enabledList.stream().anyMatch(g -> "DisabledGroup".equals(g.getName())));
    }

    @Test
    @Transactional
    public void testFindAll() {
        List<SnmpCollectionMibGroup> all = mibGroupDao.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(mg -> "Mib-Group-1".equals(mg.getName())));
    }
}

