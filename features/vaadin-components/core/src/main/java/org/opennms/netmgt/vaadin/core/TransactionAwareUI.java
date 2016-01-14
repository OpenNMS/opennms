package org.opennms.netmgt.vaadin.core;

import com.vaadin.ui.UI;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

import java.util.Objects;

public abstract class TransactionAwareUI extends UI {

    private final TransactionOperations transactionOperations;

    public TransactionAwareUI(final TransactionOperations transactionOperations) {
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
    }

    public <T> T runInTransaction(TransactionCallback<T> callback) throws TransactionException {
        return this.transactionOperations.execute(callback);
    }

    public void runInTransaction(final Runnable callback) throws TransactionException {
        this.transactionOperations.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus transactionStatus) {
                callback.run();
            }
        });
    }

    public <T> T wrapInTransactionProxy(final T t) {
        return new TransactionAwareBeanProxyFactory(this.transactionOperations).createProxy(t);
    }
}
