package org.opennms.ovapi;

import junit.framework.TestCase;

import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.ovapi.OVsnmp.OVsnmpPdu;
import org.opennms.ovapi.OVsnmp.OVsnmpSession;
import org.opennms.ovapi.OVsnmp.ObjectID;

import com.sun.jna.Native;

public class OVsnmpSessionTest extends TestCase {
    
    static {
        Native.setProtected(true);
    }
    
    MockSnmpAgent m_agent;
    
    public void setUp() throws Exception {
//        Resource snmpData = new ClassPathResource("snmpTestData1.properties");
        
//        m_agent = MockSnmpAgent.createAgentAndRun(snmpData, "127.0.0.1/9161");
    }
    
    protected void tearDown() throws Exception {
//        m_agent.shutDownAndWait();
    }

    public void XXXtestOpenClose() throws Exception {
        OVsnmpSession sess = OVsnmpSession.open("localhost", 9162);
        sess.close();
    }
    
    public void testCreatePdu() throws Exception {
        String sysName = ".1.3.6.1.2.1.1.5.0";
        
        ObjectID oid = new ObjectID();
        
        int len = oid.fromString(sysName);
        
        
        OVsnmpPdu pdu = OVsnmpPdu.createGet();
        
        System.err.println(pdu);
        
        pdu.addNullVarBind(oid, len);
        
        System.err.println(pdu);

        Thread.sleep(3000);
        
        pdu.free();

    }
    
    public OVsnmp ovsnmp() {
        return OVsnmp.INSTANCE;
    }
    

}
