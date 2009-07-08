/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.mock;

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
