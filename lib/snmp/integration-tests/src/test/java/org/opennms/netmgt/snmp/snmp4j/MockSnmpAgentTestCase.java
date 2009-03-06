package org.opennms.netmgt.snmp.snmp4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

public abstract class MockSnmpAgentTestCase extends TestCase {
    private InetAddress m_agentAddress;
    private int m_agentPort = 1691;
    private ClassPathResource m_propertiesResource = new ClassPathResource("loadSnmpDataTest.properties");
    
    private MockSnmpAgent m_agent;

    public MockSnmpAgentTestCase() {
        super();

        try {
            m_agentAddress = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            fail(e.toString());
        }
    }

    @Override
    protected void setUp() throws Exception {
        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");

        super.setUp();
        
        MockLogAppender.setupLogging();

        m_agent = MockSnmpAgent.createAgentAndRun(m_propertiesResource, m_agentAddress.getHostAddress() + "/" + m_agentPort);
    }

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Override
    protected void tearDown() throws Exception {
        if (m_agent != null) {
            m_agent.shutDownAndWait();
        }
    
        super.tearDown();

        MockUtil.println("------------ End Test " + getName() + " --------------------------");
    }

    protected SnmpAgentConfig getAgentConfig() {
        SnmpAgentConfig config = new SnmpAgentConfig();
        config.setAddress(m_agentAddress);
        config.setPort(m_agentPort);
        config.setVersion(SnmpAgentConfig.VERSION1);
        return config;
    }

    public InetAddress getAgentAddress() {
        return m_agentAddress;
    }

    public void setAgentAddress(InetAddress agentAddress) {
        m_agentAddress = agentAddress;
    }

    public int getAgentPort() {
        return m_agentPort;
    }

    public void setAgentPort(int agentPort) {
        m_agentPort = agentPort;
    }

    public ClassPathResource getPropertiesResource() {
        return m_propertiesResource;
    }

    public void setPropertiesResource(ClassPathResource propertiesResource) {
        m_propertiesResource = propertiesResource;
    }

    public MockSnmpAgent getAgent() {
        return m_agent;
    }
}