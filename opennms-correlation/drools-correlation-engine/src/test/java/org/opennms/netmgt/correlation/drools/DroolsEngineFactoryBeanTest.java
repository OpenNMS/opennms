package org.opennms.netmgt.correlation.drools;

import org.opennms.core.utils.PropertiesUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.correlation.CorrelationEngine;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.util.StringUtils;

public class DroolsEngineFactoryBeanTest extends AbstractDependencyInjectionSpringContextTests {

    public DroolsEngineFactoryBeanTest() {
        System.setProperty("opennms.home", PropertiesUtils.substitute("${user.dir}/src/test/opennms-home", System.getProperties()));
    }

    private DroolsEngineFactoryBean m_droolsEngineFactoryBean;

    public void testIt() throws Exception {
        assertNotNull(m_droolsEngineFactoryBean);
        assertTrue(m_droolsEngineFactoryBean.getObject() instanceof CorrelationEngine[]);
        CorrelationEngine[] engines = (CorrelationEngine[])m_droolsEngineFactoryBean.getObject();
        assertEquals(1, engines.length);
        assertTrue(engines[0] instanceof DroolsCorrelationEngine);
        DroolsCorrelationEngine engine = (DroolsCorrelationEngine) engines[0];
        assertEquals(2, engine.getInterestingEvents().size());
        assertTrue(engine.getInterestingEvents().contains(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI));
        assertTrue(engine.getInterestingEvents().contains(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI));
        

    }
    
    public void setDroolsEngineFactoryBean(DroolsEngineFactoryBean bean) {
        m_droolsEngineFactoryBean = bean;
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:test-context.xml",
                "classpath:META-INF/opennms/correlation-engine.xml"
        };
    }


}
