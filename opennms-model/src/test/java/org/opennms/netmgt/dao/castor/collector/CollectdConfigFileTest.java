package org.opennms.netmgt.dao.castor.collector;

import java.io.IOException;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.castor.InvocationAnticipator;
import org.springframework.core.io.ClassPathResource;

public class CollectdConfigFileTest extends TestCase {
    
    private InvocationAnticipator m_invocationAnticipator;
    private CollectdConfigVisitor m_visitor;

    protected void setUp() throws Exception {
        super.setUp();
        m_invocationAnticipator = new InvocationAnticipator(CollectdConfigVisitor.class);
        m_visitor = (CollectdConfigVisitor)m_invocationAnticipator.getProxy();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testVisitTop() throws IOException {
        
        ClassPathResource resource = new ClassPathResource("/collectdconfiguration-testdata.xml");
        CollectdConfigFile configFile = new CollectdConfigFile(resource.getFile());
        
        m_invocationAnticipator.anticipateCalls(1, "visitCollectdConfiguration");
        m_invocationAnticipator.anticipateCalls(1, "completeCollectdConfiguration");
        m_invocationAnticipator.anticipateCalls(4, "visitCollectorCollection");
        m_invocationAnticipator.anticipateCalls(4, "completeCollectorCollection");
        
        configFile.visit(m_visitor);
        
        m_invocationAnticipator.verify();
        
    }

}
