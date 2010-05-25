package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( { OpenNMSConfigurationExecutionListener.class,
        TemporaryDatabaseExecutionListener.class,
        JUnitSnmpAgentExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class })
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/importerServiceTest.xml" })
        
/* This test is for bug 3778 */
@JUnitTemporaryDatabase()
public class InvalidAssetFieldNameTest {
    
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private Provisioner m_provisioner;
    
    @Autowired
    private ResourceLoader m_resourceLoader;
    
    @Before
    public void setUp() throws Exception {
        m_provisioner.start();
    }

    @Test
    public void testImport() throws Exception{
        assertEquals(0, m_nodeDao.countAll());
        
        // This requisition has an asset on some nodes called "pollercategory".
        // Change it to "pollerCategory" (capital 'C') and the test passes...
        importResource("classpath:/import_invalidAssetFieldName.xml");
        
        assertEquals(5, m_nodeDao.countAll());

        //OnmsNode bert = m_nodeDao.findByForeignId("muppetworkshop", "bert");
        //OnmsNode ernie = m_nodeDao.findByForeignId("muppetworkshop", "ernie");
        //OnmsNode oscar = m_nodeDao.findByForeignId("muppetworkshop", "oscar");
        //OnmsNode waldorf = m_nodeDao.findByForeignId("muppetworkshop", "waldorf");
        //OnmsNode statler = m_nodeDao.findByForeignId("muppetworkshop", "statler");
    }

    private void importResource(String location) throws Exception {
        m_provisioner.importModelFromResource(m_resourceLoader.getResource(location));
    }
}
