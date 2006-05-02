package org.opennms.netmgt.dao.castor.collector;

import java.io.IOException;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.castor.InvocationAnticipator;
import org.springframework.core.io.ClassPathResource;

public class DataCollectionConfigFileTest extends TestCase {
    
    private InvocationAnticipator m_invocationAnticipator;
    private DataCollectionVisitor m_visitor;

    protected void setUp() throws Exception {
        super.setUp();
        m_invocationAnticipator = new InvocationAnticipator(DataCollectionVisitor.class);
        m_visitor = (DataCollectionVisitor)m_invocationAnticipator.getProxy();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testVisitTop() throws IOException {
        
        ClassPathResource resource = new ClassPathResource("/datacollectionconfigfile-testdata.xml");
        DataCollectionConfigFile configFile = new DataCollectionConfigFile(resource.getFile());
        
        m_invocationAnticipator.anticipateCalls(1, "visitDataCollectionConfig");
        m_invocationAnticipator.anticipateCalls(1, "completeDataCollectionConfig");
        m_invocationAnticipator.anticipateCalls(1, "visitSnmpCollection");
        m_invocationAnticipator.anticipateCalls(1, "completeSnmpCollection");
        
        configFile.visit(m_visitor);
        
        m_invocationAnticipator.verify();
        
    }

}
