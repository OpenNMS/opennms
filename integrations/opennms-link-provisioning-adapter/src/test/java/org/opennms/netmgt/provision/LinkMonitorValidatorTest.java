package org.opennms.netmgt.provision;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.provision.adapters.link.EndPointStatusValidators.and;
import static org.opennms.netmgt.provision.adapters.link.EndPointStatusValidators.match;
import static org.opennms.netmgt.provision.adapters.link.EndPointStatusValidators.ping;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusValidator;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusValidatorFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.mock.TestSnmpValue;
import org.opennms.test.mock.EasyMockUtils;


public class LinkMonitorValidatorTest {
    
    public class DefaultValidatorTestFactory implements EndPointStatusValidatorFactory{
        
        EndPointTypeConfigContainer m_container = new EndPointTypeConfigContainer();
        
        public DefaultValidatorTestFactory(){}
        
        public EndPointStatusValidator getEndPointStatusValidatorFor(String sysOid) {
            return m_container.getConfigForSysOid(sysOid).getValidator();
        }
        
    }
    
    public abstract class EndPointImpl implements EndPoint{
        
        public SnmpValue get(String oid) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        public InetAddress getAddress() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        public SnmpValue getSysOid() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        public boolean ping() {
            throw new UnsupportedOperationException("Not yet implemented");
        }
        
    }
    
    public class SnmpEndPoint extends EndPointImpl{
        SnmpAgentConfig m_agentConfig;
        
        public SnmpEndPoint(SnmpAgentConfig agentConfig) {
            m_agentConfig = agentConfig;
        }
        
        @Override
        public SnmpValue get(String oid) {
            SnmpObjId objId = SnmpObjId.get(oid);
            return SnmpUtils.get(m_agentConfig, objId);
        }

        @Override
        public InetAddress getAddress() {
            return super.getAddress();
        }

        @Override
        public SnmpValue getSysOid() {
            return super.getSysOid();
        }


    }
    
    public class PingableEndPoint extends EndPointImpl{

        @Override
        public boolean ping() {
            return super.ping();
        }

        
    }
    
    public class EndPointFactory{
        public static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";
        public EndPoint createEndPoint(MonitoredService svc) {
            
            //SnmpAgentConfig agentConfig = (SnmpAgentConfig) svc.getNetInterface().getAttribute(SNMP_AGENTCONFIG_KEY);
            
            return m_mockEndPoint;
            
        }
    }
    
    public class EndPointTypeConfigContainer{
        List<EndPointTypeConfig> m_endPointConfigs = new ArrayList<EndPointTypeConfig>();
        
        public EndPointTypeConfigContainer() {
            m_endPointConfigs.add(new EndPointTypeConfig(".1.3.6.1.4.1.7262.1", and( match(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, "^1$"), match(AIR_PAIR_R3_DUPLEX_MISMATCH, "^1$") )));
            m_endPointConfigs.add(new EndPointTypeConfig(".1.3.6.1.4.1.7262.1", and( match(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, "^1$"), match(AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, "^1$") )));
            m_endPointConfigs.add(new EndPointTypeConfig(".1.3.6.1.4.1.7262.2.2", and( match(HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL, "^1$"), match(AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, "^1$") )));
            m_endPointConfigs.add(new EndPointTypeConfig(".1.3.6.1.4.1.7262.2.3", and( match(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, "^1$"), match(AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, "^1$") )));
            m_endPointConfigs.add(new EndPointTypeConfig(".1.2.3.4", ping()));
        }
        
        public EndPointTypeConfig getConfigForSysOid(String sysOid) {
            for(EndPointTypeConfig config : m_endPointConfigs) {
                if(config.getSysOid().equals(sysOid)) {
                    return config;
                }
            }
            return null;
        }
    }
    
    public class EndPointTypeConfig {
        
        private String m_sysOid;
        private EndPointStatusValidator m_validator;
        
        public EndPointTypeConfig(String sysOid, EndPointStatusValidator validator) {
            setSysOid(sysOid);
            setValidator(validator);
        }


        public EndPointStatusValidator getValidator() {
            return m_validator;
        }


        public void setValidator(EndPointStatusValidator validator) {
            m_validator = validator;
        }


        public String getSysOid() {
            return m_sysOid;
        }


        public void setSysOid(String sysOid) {
            m_sysOid = sysOid;
        }

    }
    
    public interface SnmpAgentValueGetter{
        public SnmpValue get(String oid);
    }
    
    public interface EndPoint{
        public SnmpValue get(String oid);
        public SnmpValue getSysOid();
        public InetAddress getAddress();
        public boolean ping();
    }
    
    private static final String AIR_PAIR_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.1.19.3.1.0";
    private static final String AIR_PAIR_R3_DUPLEX_MISMATCH = ".1.3.6.1.4.1.7262.1.19.2.3.0";
    private static final String AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.1.19.3.1.0";
    private static final String HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.2.2.8.4.4.1.0";
    private static final String HORIZON_COMPACT_ETHERNET_LINK_DOWN = ".1.3.6.1.4.1.7262.2.2.8.3.1.9.0";
    private static final String HORIZON_DUO_SYSTEM_CAPACITY = ".1.3.6.1.4.1.7262.2.3.1.1.5.0";
    private static final String HORIZON_DUO_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.2.3.7.4.1.1.1.2";
    
    EasyMockUtils m_easyMock = new EasyMockUtils();
    EndPoint m_mockEndPoint;
    DefaultValidatorTestFactory m_defaultValidatorFactory;
    
    
    @Before
    public void setup() {
        m_defaultValidatorFactory = new DefaultValidatorTestFactory();
        m_mockEndPoint = createMock(EndPoint.class);

    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    public void dwoTestAirPair3Validator(){
        expect(m_mockEndPoint.get(AIR_PAIR_MODEM_LOSS_OF_SIGNAL)).andStubReturn(TestSnmpValue.parseMibValue("STRING: 1"));
        expect(m_mockEndPoint.get(AIR_PAIR_R3_DUPLEX_MISMATCH)).andStubReturn(TestSnmpValue.parseMibValue("STRING: 1"));
        
        EndPointStatusValidator validator = m_defaultValidatorFactory.getEndPointStatusValidatorFor(".1.3.6.1.4.1.7262.1");
        
        replay();
        
        assertTrue(validator.validate(m_mockEndPoint));
        
        verify();
    }
    
    @Test
    public void dwoTestAirPair3FailingValidator(){
        expect(m_mockEndPoint.get(AIR_PAIR_MODEM_LOSS_OF_SIGNAL)).andStubReturn(TestSnmpValue.parseMibValue("STRING: 2"));
        expect(m_mockEndPoint.get(AIR_PAIR_R3_DUPLEX_MISMATCH)).andStubReturn(TestSnmpValue.parseMibValue("STRING: 1"));
        
        EndPointStatusValidator validator = m_defaultValidatorFactory.getEndPointStatusValidatorFor(".1.3.6.1.4.1.7262.1");
        
        replay();
        
        assertFalse(validator.validate(m_mockEndPoint));
        
        verify();
    }
    
    @Test
    public void dwoTestPingEndPointFailed() {
        expect(m_mockEndPoint.ping()).andReturn(false);
        
        EndPointStatusValidator validator = m_defaultValidatorFactory.getEndPointStatusValidatorFor(".1.2.3.4");
        
        replay();
        
        assertFalse(validator.validate(m_mockEndPoint));
        
        verify();
    }
    
    @Test
    public void dwoTestPingEndPointSuccess() {
        expect(m_mockEndPoint.ping()).andReturn(true);
        
        EndPointStatusValidator validator = m_defaultValidatorFactory.getEndPointStatusValidatorFor(".1.2.3.4");
        
        replay();
        
        assertTrue(validator.validate(m_mockEndPoint));
        
        verify();
    }
    
    @Test
    public void dwoTestFactory() {
        
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
