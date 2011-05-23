package org.opennms.netmgt.dao.hibernate;

import org.opennms.test.DaoTestConfigBean;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class InvdConfigurationExecutionListener extends AbstractTestExecutionListener {
	
	public void prepareTestInstance(TestContext testContext) throws Exception {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();
    }

}
