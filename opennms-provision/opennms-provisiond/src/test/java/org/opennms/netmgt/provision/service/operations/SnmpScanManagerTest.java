package org.opennms.netmgt.provision.service.operations;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(JUnitSnmpAgentExecutionListener.class)
@JUnitSnmpAgent(resource="classpath:snmpTestData1.properties", port=SnmpScanManagerTest.AGENT_PORT)
public class SnmpScanManagerTest {

    public static final int AGENT_PORT = 9161;

    InetAddress m_agentAddress;
    ScanResource m_resource;
    SnmpScanManager m_scanManager;
    SnmpAgentConfig m_agentConfig;

    @Before
    public void setUp() throws UnknownHostException, InterruptedException {
        m_agentAddress = InetAddress.getLocalHost();
        m_agentConfig = new SnmpAgentConfig(m_agentAddress);
        m_agentConfig.setPort(AGENT_PORT);

        m_scanManager = new SnmpScanManager(m_agentAddress);
        
        
    }
    
//    @Test
//    public void testIT() {
//        SnmpScanManager<SnmpAgent> scanMgr = new SnmpScanManager<SnmpAgent>();
//        snmpMgr.setAgentConfigFactory(...);
//        snmpMgr.setRetries(3);
//        snmpMgr.setTimeout(1000);
//        
//        SnmpAgent agent = ...;
//        InetAddress ipAddr = agent.getAttribute("preferredAddress", InetAddress.class);
//        
//        OnmsIpInterface ipIface = resourceFactory.createResource(node, "10.1.1.1", OnmsIpInterface.class);
//        
//        OnmsResource generice = resourceFactory.createResource("");
//        
//        snmpMgr.scan(resourceFactory, agent);
//        
//        assertEquals("1.2.3", agent.getSysObjectId());
//        assertEquals("brozow.local", agent.getSysName());
//        
//        Criteria ipCrit = Criteria.eq("ipaddr", "10.1.1.1");
//        Criteria snmpIfCrit = Criteria.eq("ifindex", "2");
//        
//        assertNotNull(agent.getResource("IP", "10.1.1.1"));
//        assertNotNull(agent.getResource("SNMP_IF", "1"));
//    }

    @Test
    @Ignore
    public void testCreateSnmpResource() {
        
        ScanResource sr = new ScanResource("SNMP");
        OnmsNode n = new OnmsNode();
        n.setLabel("test");
        m_scanManager.updateSnmpDataForResource(sr);
        assertEquals("test@example.com", n.getSysContact());
        
    }
}
