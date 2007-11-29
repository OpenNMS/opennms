package org.opennms.netmgt.capsd;

import java.io.File;

import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.OpenNMSIntegrationTestCase;
import org.springframework.core.io.ClassPathResource;

public class CapsdIntegrationTest extends OpenNMSIntegrationTestCase {
    
    private static final int FOREIGN_NODEID = 77;

    private Capsd m_capsd;
    private MockSnmpAgent m_agent;

    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:META-INF/opennms/applicationContext-dao.xml",
                "classpath:META-INF/opennms/applicationContext-daemon.xml",
                "classpath:META-INF/opennms/applicationContext-commonConfigs.xml",
                "classpath:META-INF/opennms/applicationContext-capsd.xml"
        };
    }
    
    public void setCapsd(Capsd capsd) {
        m_capsd = capsd;
    }
    
    @Override
    protected MockNetwork createMockNetwork() {
        MockNetwork network = super.createMockNetwork();
        
        network.addNode(FOREIGN_NODEID, "ForeignNode");
        network.addInterface("172.20.1.201");
        network.addService("ICMP");
        network.addService("SNMP");
        
        return network;
    }

    
    
    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("org/opennms/netmgt/snmp/snmpTestData1.properties"), getLocalHostAddress()+"/9161");
    }
    
    

    @Override
    protected void onTearDownAfterTransaction() throws Exception {
        if (m_agent != null) {
            m_agent.shutDownAndWait();
        }
    }

    @Override
    protected String preprocessConfigContents(File srcFile, String contents) {
        if (srcFile.getName().matches("snmp-config.xml")) {
            return getSnmpConfig();
        } else if (srcFile.getName().matches("capsd-configuration.xml")) {
            String updatedContents = contents.replaceAll("initial-sleep-time=\"30000\"", "initial-sleep-time=\"300\"");
            updatedContents = updatedContents.replaceAll("scan=\"on\"", "scan=\"off\"");
            updatedContents = updatedContents.replaceAll("SnmpPlugin\" scan=\"off\"", "SnmpPlugin\" scan=\"on\"");
            return updatedContents;
        } else {
            return contents;
        }
    }

    public String getSnmpConfig() {
        return "<?xml version=\"1.0\"?>\n" + 
                "<snmp-config "+ 
                " retry=\"3\" timeout=\"3000\"\n" + 
                " read-community=\"public\"" +
                " write-community=\"private\"\n" + 
                " port=\"161\"\n" +
                " version=\"v1\">\n" +
                "   <definition version=\"v2c\" port=\"9161\" read-community=\"public\" proxy-host=\""+getLocalHostAddress()+"\">\n" + 
                "      <specific>172.20.1.201</specific>\n" +
                "      <specific>172.20.1.204</specific>\n" +
                "   </definition>\n" + 
                "</snmp-config>\n";
    }
 
    public final void testRescan() throws Exception {
        
        assertEquals("Initially only 1 interface", 1, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));

        m_capsd.start();
        
        m_capsd.rescanInterfaceParent(77);
        
        Thread.sleep(10000);
        
        m_capsd.stop();
        
        assertEquals("after scanning should be 5 interfaces", 5, m_db.countRows("select * from ipinterface where nodeid = ?", FOREIGN_NODEID));
    }
    
 
    
    

}
