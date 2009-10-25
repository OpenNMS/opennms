package org.opennms.netmgt.provision;

import static org.easymock.EasyMock.expect;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.DefaultEndPointConfigurationDao;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.EndPointConfigurationDao;
import org.opennms.netmgt.snmp.mock.TestSnmpValue;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.core.io.ClassPathResource;

public class LinkMonitorValidatorTest {
    
    public static class EndPointFactory {
        public static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";
        public EndPoint createEndPoint(MonitoredService svc) {
            return m_mockEndPoint;
            
        }
    }
    
    public static final String AIR_PAIR_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.1.19.3.1.0";
    public static final String AIR_PAIR_R3_DUPLEX_MISMATCH = ".1.3.6.1.4.1.7262.1.19.2.3.0";
    public static final String AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.1.19.3.1.0";
    public static final String HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.2.2.8.4.4.1.0";
    public static final String HORIZON_COMPACT_ETHERNET_LINK_DOWN = ".1.3.6.1.4.1.7262.2.2.8.3.1.9.0";
    public static final String HORIZON_DUO_SYSTEM_CAPACITY = ".1.3.6.1.4.1.7262.2.3.1.1.5.0";
    public static final String HORIZON_DUO_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.2.3.7.4.1.1.1.2";
    
    EasyMockUtils m_easyMock = new EasyMockUtils();
    static EndPoint m_mockEndPoint;
    EndPointConfigurationDao m_configDao;
    
    @Before
    public void setup() {
        DefaultEndPointConfigurationDao dao = new DefaultEndPointConfigurationDao();
        dao.setConfigResource(new ClassPathResource("/test-endpoint-configuration.xml"));
        dao.afterPropertiesSet();
        m_configDao = dao;
        
        m_mockEndPoint = createMock(EndPoint.class);

    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    public void dwoTestAirPair3Validator() throws Exception {
        expect(m_mockEndPoint.get(AIR_PAIR_MODEM_LOSS_OF_SIGNAL)).andStubReturn(TestSnmpValue.parseMibValue("STRING: 1"));
        expect(m_mockEndPoint.get(AIR_PAIR_R3_DUPLEX_MISMATCH)).andStubReturn(TestSnmpValue.parseMibValue("STRING: 1"));
        expect(m_mockEndPoint.getSysOid()).andStubReturn(".1.3.6.1.4.1.7262.1");

        replay();

        m_configDao.getValidator().validate(m_mockEndPoint);
        
        verify();
    }
    
    @Test(expected=EndPointStatusException.class)
    public void dwoTestAirPair3FailingValidator() throws Exception {
        expect(m_mockEndPoint.get(AIR_PAIR_MODEM_LOSS_OF_SIGNAL)).andStubReturn(TestSnmpValue.parseMibValue("STRING: 2"));
        expect(m_mockEndPoint.get(AIR_PAIR_R3_DUPLEX_MISMATCH)).andStubReturn(TestSnmpValue.parseMibValue("STRING: 1"));
        expect(m_mockEndPoint.getSysOid()).andStubReturn(".1.3.6.1.4.1.7262.1");
        
        replay();

        try {
            m_configDao.getValidator().validate(m_mockEndPoint);
        } finally {
            verify();
        }
    }
    
    @Test(expected=EndPointStatusException.class)
    public void dwoTestPingEndPointFailed() throws Exception {
        expect(m_mockEndPoint.getSysOid()).andStubReturn(".1.2.3.4");
        expect(m_mockEndPoint.ping()).andReturn(false);
        
        replay();

        try {
            m_configDao.getValidator().validate(m_mockEndPoint);
        } finally {
            verify();
        }
    }
    
    @Test
    public void dwoTestPingEndPointSuccess() throws Exception {
        expect(m_mockEndPoint.getSysOid()).andStubReturn(".1.2.3.4");
        expect(m_mockEndPoint.ping()).andReturn(true);
        
        replay();
        
        m_configDao.getValidator().validate(m_mockEndPoint);
        
        verify();
    }
    
    public <T> T createMock(Class<T> clazz){
        return m_easyMock.createMock(clazz);
    }
    
    public void verify(){
        m_easyMock.verifyAll();
    }
    
    public void replay(){
        m_easyMock.replayAll();
    }
}
