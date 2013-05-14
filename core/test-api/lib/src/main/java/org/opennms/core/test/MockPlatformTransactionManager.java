/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.test;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class MockPlatformTransactionManager implements
        PlatformTransactionManager {

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) {
        return new TransactionStatus() {

            @Override
            public boolean hasSavepoint() {
                throw new UnsupportedOperationException(".hasSavepoint not yet implemented.");
            }

            @Override
            public boolean isCompleted() {
                throw new UnsupportedOperationException(".isCompleted not yet implemented.");
            }

            @Override
            public boolean isNewTransaction() {
                throw new UnsupportedOperationException(".isNewTransaction not yet implemented.");
            }

            @Override
            public boolean isRollbackOnly() {
                throw new UnsupportedOperationException(".isRollbackOnly not yet implemented.");
            }

            @Override
            public void setRollbackOnly() {
                throw new UnsupportedOperationException(".setRollbackOnly not yet implemented.");
            }

            @Override
            public Object createSavepoint() throws TransactionException {
                throw new UnsupportedOperationException(".createSavepoint not yet implemented.");
            }

            @Override
            public void releaseSavepoint(Object savepoint) throws TransactionException {
                throw new UnsupportedOperationException(".releaseSavepoint not yet implemented.");
            }

            @Override
            public void rollbackToSavepoint(Object savepoint) throws TransactionException {
                throw new UnsupportedOperationException(".rollbackToSavepoint not yet implemented.");
            }

            @Override
            public void flush() {
                throw new UnsupportedOperationException(".flush not yet implemented.");
            }
        };
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
    }

}
