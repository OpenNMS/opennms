/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

public class MockTransactionManager implements PlatformTransactionManager {

    public MockTransactionManager() {
    }

    @Override
    public TransactionStatus getTransaction(final TransactionDefinition definition) throws TransactionException {
        return new TransactionStatus() {
            private boolean m_rollbackOnly = false;

            @Override
            public Object createSavepoint() throws TransactionException {
                //System.err.println("createSavepoint");
                return null;
            }

            @Override
            public void rollbackToSavepoint(final Object savepoint) throws TransactionException {
                //System.err.println("rollbackToSavepoint");
            }

            @Override
            public void releaseSavepoint(final Object savepoint) throws TransactionException {
                //System.err.println("releaseSavepoint");
            }

            @Override
            public boolean isNewTransaction() {
                //System.err.println("isNewTransaction");
                return true;
            }

            @Override
            public boolean hasSavepoint() {
                //System.err.println("hasSavepoint");
                return false;
            }

            @Override
            public void setRollbackOnly() {
                //System.err.println("setRollbackOnly");
                m_rollbackOnly = true;
            }

            @Override
            public boolean isRollbackOnly() {
                //System.err.println("isRollbackOnly");
                return m_rollbackOnly;
            }

            @Override
            public void flush() {
                //System.err.println("flush");
            }

            @Override
            public boolean isCompleted() {
                //System.err.println("isCompleted");
                return true;
            }
            
        };
    }

    @Override
    public void commit(final TransactionStatus status) throws TransactionException {
        //System.err.println("commit");
    }

    @Override
    public void rollback(final TransactionStatus status) throws TransactionException {
        //System.err.println("rollback");
    }

}
