package org.opennms.netmgt.provision.persist.policies;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={ "classpath:/META-INF/opennms/applicationContext-dao.xml", "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })

@JUnitTemporaryDatabase()
public class IPManagementMatchPolicyTest {
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private DatabasePopulator m_populator;

    @Before
    public void setUp() {
        m_populator.populateDatabase();
    }
    
    @Test
    @Transactional
    public void testPolicy() {
        OnmsIpInterface o = null;
        IPManagementMatchPolicy p = new IPManagementMatchPolicy();

        List<OnmsIpInterface> interfaces = m_ipInterfaceDao.findAll();
        List<OnmsIpInterface> matchedInterfaces = new ArrayList<OnmsIpInterface>();
        
        for (OnmsIpInterface iface : interfaces) {
            o = p.apply(iface);
            if (o != null) {
                matchedInterfaces.add(o);
            }
        }
        
        assertEquals(interfaces, matchedInterfaces);
    }

}
