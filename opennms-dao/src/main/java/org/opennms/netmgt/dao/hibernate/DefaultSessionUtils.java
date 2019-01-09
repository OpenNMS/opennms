/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Supplier;

import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;

public class DefaultSessionUtils implements SessionUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSessionUtils.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TransactionOperations transactionOperations;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private static final DefaultTransactionDefinition readOnlyTransactionDefinition = new DefaultTransactionDefinition();
    static {
        readOnlyTransactionDefinition.setReadOnly(true);
    }

    @Override
    public <V> V withTransaction(Supplier<V> supplier) {
        return transactionOperations.execute(status -> supplier.get());
    }

    @Override
    public <V> V withReadOnlyTransaction(Supplier<V> supplier) {
        return executeWithTransactionDefinition(readOnlyTransactionDefinition, () -> withManualFlush(supplier));
    }

    @Override
    public <V> V withManualFlush(Supplier<V> supplier) {
        final FlushMode flushMode = sessionFactory.getCurrentSession().getFlushMode();
        try {
            sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);
            return supplier.get();
        } finally {
            sessionFactory.getCurrentSession().setFlushMode(flushMode);
        }
    }

    /**
     * NOTE: This is mostly copied from org.springframework.transaction.support.TransactionTemplate#execute
     * but adds the ability to specify our own transaction definition.
     */
    private <V> V executeWithTransactionDefinition(TransactionDefinition transactionDefinition, Supplier<V> supplier) {
        final TransactionStatus status = transactionManager.getTransaction(transactionDefinition);
        V result;
        try {
            result = supplier.get();
        }
        catch (RuntimeException ex) {
            // Transactional code threw application exception -> rollback
            rollbackOnException(status, ex);
            throw ex;
        }
        catch (Error err) {
            // Transactional code threw error -> rollback
            rollbackOnException(status, err);
            throw err;
        }
        catch (Throwable ex) {
            // Transactional code threw unexpected exception -> rollback
            rollbackOnException(status, ex);
            throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
        }
        this.transactionManager.commit(status);
        return result;
    }

    /**
     * NOTE: This is a copy of org.springframework.transaction.support.TransactionTemplate#rollbackOnException
     *
     * Perform a rollback, handling rollback exceptions properly.
     * @param status object representing the transaction
     * @param ex the thrown application exception or error
     * @throws TransactionException in case of a rollback error
     */
    private void rollbackOnException(TransactionStatus status, Throwable ex) throws TransactionException {
        LOG.debug("Initiating transaction rollback on application exception", ex);
        try {
            this.transactionManager.rollback(status);
        }
        catch (TransactionSystemException ex2) {
            LOG.error("Application exception overridden by rollback exception", ex);
            ex2.initApplicationException(ex);
            throw ex2;
        }
        catch (RuntimeException ex2) {
            LOG.error("Application exception overridden by rollback exception", ex);
            throw ex2;
        }
        catch (Error err) {
            LOG.error("Application exception overridden by rollback error", ex);
            throw err;
        }
    }
}
