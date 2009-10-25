package org.opennms.netmgt.provision;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.provision.adapters.link.EndPointStatusValidators.match;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusValidator;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusValidatorFactory;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.test.mock.EasyMockUtils;


public class LinkMonitorValidatorTest {
    
    public class DefaultValidatorTestFactory implements EndPointStatusValidatorFactory{

        public EndPointStatusValidator getEndPointStatusValidatorFor(String sysOid) {
            return match(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, "^1$");
        }
        
    }
    
    
    public interface SnmpAgentValueGetter{
        public SnmpValue get(String oid);
    }
    
    private static final String AIR_PAIR_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.1.19.3.1.0";
    EasyMockUtils m_easyMock = new EasyMockUtils();
    SnmpAgentValueGetter m_mockSnmpValueGetter;
    DefaultValidatorTestFactory m_defaultValidatorFactory;
    
    @Before
    public void setup() {
        m_defaultValidatorFactory = new DefaultValidatorTestFactory();
        
        m_mockSnmpValueGetter = createMock(SnmpAgentValueGetter.class);
        expect(m_mockSnmpValueGetter.get(AIR_PAIR_MODEM_LOSS_OF_SIGNAL)).andStubReturn(null);
    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    public void dwoTestValidator(){
        EndPointStatusValidator validator = m_defaultValidatorFactory.getEndPointStatusValidatorFor(".1.3.6.1.4.1.7262.1");
        
        assertTrue(validator.validate(m_mockSnmpValueGetter));
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
