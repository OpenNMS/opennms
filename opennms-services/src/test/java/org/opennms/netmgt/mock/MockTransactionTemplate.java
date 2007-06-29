package org.opennms.netmgt.mock;

import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class MockTransactionTemplate extends TransactionTemplate {

    @Override
    public Object execute(TransactionCallback action) throws TransactionException {
        return action.doInTransaction(new TransactionStatus() {

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
            
        });
    }

    private static final long serialVersionUID = 1L;
    
    

}
