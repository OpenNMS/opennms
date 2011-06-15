package org.opennms.netmgt.provision.persist.policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class CollectionPolicyTest {
    
    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    private DatabasePopulator m_populator;

    private List<OnmsSnmpInterface> m_interfaces;
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_populator.populateDatabase();
        m_interfaces = m_snmpInterfaceDao.findAll();
    }
    
    @Test
    @Transactional
    public void testMatchingIfDescr() {
        MatchingSnmpInterfacePolicy p = createPolicy();
        p.setIfDescr("~^ATM.*");

        matchPolicy(m_interfaces, p, addr("192.168.1.1"));
    }

    private MatchingSnmpInterfacePolicy createPolicy() {
        MatchingSnmpInterfacePolicy policy = new MatchingSnmpInterfacePolicy();
        policy.setMatchBehavior(BasePolicy.Match.NO_PARAMETERS.toString());
        return policy;
    }

    @Test
    @Transactional
    public void testMatchingIfName() {
        MatchingSnmpInterfacePolicy p = createPolicy();
        p.setIfName("eth0");

        matchPolicy(m_interfaces, p, addr("192.168.1.2"));
    }

    @Test
    @Transactional
    public void testMatchingIfType() {
        MatchingSnmpInterfacePolicy p = createPolicy();
        p.setIfType("6");

        matchPolicy(m_interfaces, p, addr("192.168.1.2"));
    }
    
    @Test
    @Transactional
    public void testCategoryAssignment() {
        final String TEST_CATEGORY = "TestCategory"; 
        NodeCategorySettingPolicy policy = new NodeCategorySettingPolicy();
        policy.setCategory(TEST_CATEGORY);
        policy.setLabel("~n.*2");
        
        OnmsNode node1 = m_nodeDao.get(1);
        assertNotNull(node1);
        assertEquals("node1", node1.getLabel());
        
        OnmsNode node2 = m_nodeDao.get(2);
        assertNotNull(node2);
        assertEquals("node2", node2.getLabel());
        
        node1 = policy.apply(node1);
        assertNotNull(node1);
        assertFalse(node1.hasCategory(TEST_CATEGORY));
        
        node2 = policy.apply(node2);
        assertNotNull(node1);
        assertTrue(node2.hasCategory(TEST_CATEGORY));
        
        
        
        
        
        
    }

    private static void matchPolicy(List<OnmsSnmpInterface> interfaces, MatchingSnmpInterfacePolicy p, InetAddress matchingIp) {
        OnmsSnmpInterface o;
        List<OnmsSnmpInterface> populatedInterfaces = new ArrayList<OnmsSnmpInterface>();
        List<OnmsSnmpInterface> matchedInterfaces = new ArrayList<OnmsSnmpInterface>();
        
        for (OnmsSnmpInterface iface : interfaces) {
            System.err.println(iface);
            o = p.apply(iface);
            if (o != null) {
                matchedInterfaces.add(o);
            }
            for (OnmsIpInterface ipif : iface.getIpInterfaces()) {
                if (ipif.getIpAddress().equals(matchingIp)) {
                    populatedInterfaces.add(iface);
                }
            }
        }
        
        assertEquals(populatedInterfaces, matchedInterfaces);
    }

}
