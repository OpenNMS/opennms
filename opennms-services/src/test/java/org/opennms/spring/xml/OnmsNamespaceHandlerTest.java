package org.opennms.spring.xml;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class OnmsNamespaceHandlerTest extends AbstractDependencyInjectionSpringContextTests {

    private TestDaemon m_testDaemon;
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/org/opennms/spring/xml/applicationContext-testNamespace.xml"
        };
    }
    
    public void setTestDaemon(TestDaemon testDaemon) {
        m_testDaemon = testDaemon;
    }
    
    public void testServiceTag() {
        assertNotNull(m_testDaemon);
        
        assertEquals("attrFromBeansFile", m_testDaemon.getAttr());
    }

}
