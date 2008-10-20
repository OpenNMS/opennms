package org.opennms.netmgt.provision.scan.snmp;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.provision.ScanContext;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.snmp4j.MockSnmpAgentTestCase;

import com.mchange.util.AssertException;

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
        
        //MockSnmpAgent m_agent = MockSnmpAgent.
        
        m_agentConfig = new SnmpAgentConfig(m_agentAddress);
        
        m_scanContext = new MockScanContext();

    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testScan() throws Exception {

        SnmpNodeScanner scanner = new SnmpNodeScanner();
        scanner.setSnmpAgentConfigFactory(snmpAgentConfigFactory());
        scanner.init();
        scanner.scan(m_scanContext);
        
        assertEquals("what should this be?", m_scanContext.getSysObjectId());
        
    }

}
