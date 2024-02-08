/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
