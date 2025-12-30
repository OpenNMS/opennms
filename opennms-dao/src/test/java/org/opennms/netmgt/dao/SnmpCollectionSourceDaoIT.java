package org.opennms.netmgt.dao;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
public class SnmpCollectionSourceDaoIT {

    @Autowired
    private SnmpCollectionSourceDao snmpDao;

    @Autowired
    private SessionFactory sessionFactory;

    private SnmpCollectionSource source;

    @Before
    @Transactional
    public void setUp() {
        // Clean up previous sources
        snmpDao.deleteAll(snmpDao.findAll());
        snmpDao.flush();

        // Add default SNMP source
        source = new SnmpCollectionSource();
        source.setName("JUnit Source");
        source.setEnabled(true);
        source.setDescription("JUnit Description");
        source.setCreatedTime(new Date());
        source.setLastModified(new Date());
        snmpDao.saveOrUpdate(source);
        snmpDao.flush();
    }

    @After
    @Transactional
    public void tearDown() {
        snmpDao.deleteAll(snmpDao.findAll());
        snmpDao.flush();
    }

    @Test
    @Transactional
    public void testFindByName() {
        SnmpCollectionSource found = snmpDao.findByName("JUnit Source");
        assertNotNull(found);
        assertEquals("JUnit Description", found.getDescription());
    }

    @Test
    @Transactional
    public void testEnabledIsPersisted() {
        SnmpCollectionSource found = snmpDao.findByName("JUnit Source");
        assertNotNull(found);
        assertTrue(found.getEnabled());
    }

    @Test
    @Transactional
    public void testGetById() {
        SnmpCollectionSource found = snmpDao.get(source.getId());
        assertNotNull(found);
        assertEquals(source.getName(), found.getName());
    }

    @Test
    @Transactional
    public void testFindAllEnabled() {
        List<SnmpCollectionSource> enabledList = snmpDao.findAllEnabled();
        assertFalse(enabledList.isEmpty());
        assertTrue(enabledList.stream().anyMatch(s -> "JUnit Source".equals(s.getName())));
    }

    @Test
    @Transactional
    public void testFindByNameReturnsNullIfNotExist() {
        SnmpCollectionSource found = snmpDao.findByName("Nonexistent Source");
        assertNull(found);
    }

    @Test
    @Transactional
    public void testFindAllEnabledOnlyReturnsEnabled() {
        SnmpCollectionSource disabled = new SnmpCollectionSource();
        disabled.setName("Disabled Source");
        disabled.setEnabled(false);
        disabled.setDescription("Should not appear in enabled list");
        disabled.setCreatedTime(new Date());
        disabled.setLastModified(new Date());
        snmpDao.saveOrUpdate(disabled);
        snmpDao.flush();

        List<SnmpCollectionSource> enabledList = snmpDao.findAllEnabled();
        assertTrue(enabledList.stream().allMatch(SnmpCollectionSource::getEnabled));
        assertFalse(enabledList.stream().anyMatch(s -> "Disabled Source".equals(s.getName())));
    }

    @Test
    @Transactional
    public void testFindAll() {
        List<SnmpCollectionSource> all = snmpDao.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
    }
}
