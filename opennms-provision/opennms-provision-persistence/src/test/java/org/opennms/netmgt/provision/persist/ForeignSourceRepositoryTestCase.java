package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/testForeignSourceContext.xml"
})
@JUnitConfigurationEnvironment
public abstract class ForeignSourceRepositoryTestCase implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void configureLogging() {
        MockLogAppender.setupLogging();
    }

    protected void assertRequisitionsMatch(final String msg, final Requisition a, final Requisition b) {
        assertEquals(msg, a.getForeignSource(), b.getForeignSource());
        assertEquals(msg, a.getNodes(), b.getNodes());
    }
}
