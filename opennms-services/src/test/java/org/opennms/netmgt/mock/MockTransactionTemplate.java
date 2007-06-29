package org.opennms.netmgt.mock;

import org.springframework.transaction.support.TransactionTemplate;

public class MockTransactionTemplate extends TransactionTemplate {
    private static final long serialVersionUID = 1L;
    

    public MockTransactionTemplate() {
        super(new MockPlatformTransactionManager());
    }

}
