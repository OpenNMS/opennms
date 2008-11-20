package org.opennms.netmgt.provision.service.operations;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.core.io.ClassPathResource;

public class ScanManagerTest {

    InetAddress m_agentAddress;
    ScanResource m_resource;
    ScanManager m_scanManager;
    SnmpAgentConfig m_agentConfig;
    MockSnmpAgent m_agent;
    private static final Integer AGENT_PORT = 9161;

    @Before
    public void setUp() throws UnknownHostException, InterruptedException {
        m_agentAddress = InetAddress.getLocalHost();
        m_agent = MockSnmpAgent.createAgentAndRun(
            new ClassPathResource("org/opennms/netmgt/provision/scan/snmp/snmpTestData1.properties"),
            m_agentAddress.getHostAddress()+"/"+AGENT_PORT
        );
        m_agentConfig = new SnmpAgentConfig(m_agentAddress);
        m_agentConfig.setPort(AGENT_PORT);

        m_scanManager = new ScanManager(m_agentAddress);
    }

    @After
    public void tearDown() throws InterruptedException {
        m_agent.shutDownAndWait();
    }
    
    @Ignore
    @Test
    public void testCreateSnmpResource() {
        ScanResource sr = new ScanResource("SNMP");
        OnmsNode n = new OnmsNode();
        n.setLabel("test");
        m_scanManager.updateSnmpDataForResource(sr);
        assertEquals("test@example.com", n.getSysContact());
    }
}
