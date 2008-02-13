package org.opennms.ovapi;

import junit.framework.TestCase;

import org.opennms.nnm.swig.NNM;
import org.opennms.nnm.swig.OVsnmpSession;

public class OVsnmpSessionTest extends TestCase {
    
    //MockSnmpAgent m_agent;
    
    public void setUp() throws Exception {
//        Resource snmpData = new ClassPathResource("snmpTestData1.properties");
        
//        m_agent = MockSnmpAgent.createAgentAndRun(snmpData, "127.0.0.1/9161");
    }
    
    protected void tearDown() throws Exception {
//        m_agent.shutDownAndWait();
    }

    public void testOpenClose() throws Exception {

        OVsnmpSession sess = open("localhost", 9162);
        assertNotNull(sess);
        close(sess);
    }
    
    OVsnmpSession open(String peername, int remotePort) {
        return NNM.OVsnmpOpen(NNM.SNMP_USE_DEFAULT_COMMUNITY, "localhost", NNM.SNMP_USE_DEFAULT_RETRIES, 
                NNM.SNMP_USE_DEFAULT_INTERVAL, NNM.SNMP_USE_DEFAULT_LOCAL_PORT, remotePort, null);
        
    }
    
    void close(OVsnmpSession session) {
        NNM.OVsnmpClose(session);
    }
    
    public void testCreatePdu() throws Exception {
        String sysName = ".1.3.6.1.2.1.1.5.0";
        
        //ObjectID oid = new ObjectID();
        
        //int len = oid.fromString(sysName);
        
        //System.err.println("len = " + len + " oid = " + oid.toString(len));
        
        //OVsnmpPdu pdu = OVsnmpPdu.createGet();
        
        //System.err.println(pdu);
        
        //pdu.addNullVarBind(oid, len);
        
        //System.err.println(pdu);

        //Thread.sleep(3000);
        
        //pdu.free();

    }
    
    public OVsnmp ovsnmp() {
        return OVsnmp.INSTANCE;
    }
    

}
