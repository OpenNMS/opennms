package org.opennms.netmgt.mock;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

public class MockPlatformTransactionManager implements
        PlatformTransactionManager {

    public void commit(TransactionStatus status) throws TransactionException {
    }

    public TransactionStatus getTransaction(TransactionDefinition definition) {
        return new TransactionStatus() {

            public boolean hasSavepoint() {
                throw new UnsupportedOperationException(".hasSavepoint not yet implemented.");
            }

            public boolean isCompleted() {
                throw new UnsupportedOperationException(".isCompleted not yet implemented.");
            }

            public boolean isNewTransaction() {
                throw new UnsupportedOperationException(".isNewTransaction not yet implemented.");
            }

            public boolean isRollbackOnly() {
                throw new UnsupportedOperationException(".isRollbackOnly not yet implemented.");
            }

            public void setRollbackOnly() {
                throw new UnsupportedOperationException(".setRollbackOnly not yet implemented.");
            }

            public Object createSavepoint() throws TransactionException {
                throw new UnsupportedOperationException(".createSavepoint not yet implemented.");
            }

            public void releaseSavepoint(Object savepoint) throws TransactionException {
                throw new UnsupportedOperationException(".releaseSavepoint not yet implemented.");
            }

            public void rollbackToSavepoint(Object savepoint) throws TransactionException {
                throw new UnsupportedOperationException(".rollbackToSavepoint not yet implemented.");
            }
        };
    }

    public void rollback(TransactionStatus status) throws TransactionException {
    }

}
