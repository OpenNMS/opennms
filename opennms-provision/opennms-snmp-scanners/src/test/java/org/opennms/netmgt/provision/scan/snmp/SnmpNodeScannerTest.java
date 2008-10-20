package org.opennms.netmgt.provision.scan.snmp;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.provision.ScanContext;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.core.io.ClassPathResource;

public class SnmpNodeScannerTest {
    
    /**
     * @author brozow
     *
     */
    private final class MockScanContext implements ScanContext {
        String m_sysObjectId;

        public InetAddress getAgentAddress(String agentType) {
            return m_agentAddress;
        }

        public void updateSysObjectId(String sysObjectId) {
            m_sysObjectId = sysObjectId;
        }

        public String getSysObjectId() {
            return m_sysObjectId;
        }
    }

    private InetAddress m_agentAddress;
    private SnmpAgentConfig m_agentConfig;
    private MockScanContext m_scanContext;
    private MockSnmpAgent m_agent;
    private static final Integer AGENT_PORT = 9161;
    
    private SnmpAgentConfigFactory snmpAgentConfigFactory() {
        return snmpAgentConfigFactory(m_agentConfig);
    }
    
    private SnmpAgentConfigFactory snmpAgentConfigFactory(final SnmpAgentConfig config) {
        return new SnmpAgentConfigFactory() {

            public SnmpAgentConfig getAgentConfig(InetAddress address) {
                assertEquals(config.getAddress(), address);
                return config;
            }
            
        };
    }

    @Before
    public void setUp() throws Exception {
        m_agentAddress = InetAddress.getLocalHost();
        
        m_agent = MockSnmpAgent.createAgentAndRun(
            new ClassPathResource("org/opennms/netmgt/provision/scan/snmp/snmpTestData1.properties"),
            m_agentAddress.getHostAddress()+"/"+AGENT_PORT
        );
        
        m_agentConfig = new SnmpAgentConfig(m_agentAddress);
        m_agentConfig.setPort(AGENT_PORT);
        
        m_scanContext = new MockScanContext();

    }

    @After
    public void tearDown() throws Exception {
        m_agent.shutDownAndWait();
    }
    
    @Test
    public void testScan() throws Exception {

        SnmpNodeScanner scanner = new SnmpNodeScanner();
        scanner.setSnmpAgentConfigFactory(snmpAgentConfigFactory());
        scanner.init();
        scanner.scan(m_scanContext);
        
        assertEquals(".1.3.6.1.4.1.8072.3.2.255", m_scanContext.getSysObjectId());
        
    }

}
