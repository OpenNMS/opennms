package org.opennms.netmgt.dao.castor.collector;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.castor.InvocationAnticipator;
import org.springframework.core.io.ClassPathResource;

public class DataCollectionConfigFileTest extends TestCase {
    
    private InvocationAnticipator m_invocationAnticipator;
    private DataCollectionVisitor m_visitor;
    

    protected void setUp() throws Exception {
        super.setUp();
        
        InvocationHandler noNullsAllowed = new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                assertNotNull(args);
                assertEquals(1, args.length);
                assertNotNull(args[0]);
                return null;
            }
            
        };
        m_invocationAnticipator = new InvocationAnticipator(DataCollectionVisitor.class);
        m_invocationAnticipator.setInvocationHandler(noNullsAllowed);
        
        m_visitor = (DataCollectionVisitor)m_invocationAnticipator.getProxy();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testVisit() throws IOException {
        
        ClassPathResource resource = new ClassPathResource("/datacollectionconfigfile-testdata.xml");
        DataCollectionConfigFile configFile = new DataCollectionConfigFile(resource.getFile());
        
        anticipateVisits(1, "DataCollectionConfig");
        anticipateVisits(1, "SnmpCollection");
        anticipateVisits(1, "Rrd");
        anticipateVisits(4, "Rra");
        anticipateVisits(26, "SystemDef");
        anticipateVisits(4, "SysOid");
        anticipateVisits(22, "SysOidMask");
        anticipateVisits(0, "IpList");
        anticipateVisits(26, "Collect");
        anticipateVisits(69, "IncludeGroup");
        anticipateVisits(57, "Group");
        anticipateVisits(0, "SubGroup");
        anticipateVisits(809, "MibObj");
        
        configFile.visit(m_visitor);
        
        m_invocationAnticipator.verify();
        
    }

    private void anticipateVisits(int count, String visited) {
        m_invocationAnticipator.anticipateCalls(count, "visit"+visited);
        m_invocationAnticipator.anticipateCalls(count, "complete"+visited);
    }

}
