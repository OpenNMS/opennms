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
