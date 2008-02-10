package org.opennms.netmgt.vulnscand;

import junit.framework.TestCase;

import org.opennms.netmgt.config.VulnscandConfigFactory;
import org.opennms.test.DaoTestConfigBean;

public class SchedulerTest extends TestCase {

    public SchedulerTest() {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.setRelativeHomeDirectory("src/test/test-configurations/vulnscand");
        bean.afterPropertiesSet();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        VulnscandConfigFactory.init();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreate() throws Exception {
//      FifoQueue q = new FifoQueueImpl();
//      Scheduler scheduler = new Scheduler(q);
//      Vulnscand vulnscand = null;
//      vulnscand.initialize();
    }
}
