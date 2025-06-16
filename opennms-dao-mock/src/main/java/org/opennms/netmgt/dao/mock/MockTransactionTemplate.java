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

import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class MockTransactionTemplate extends TransactionTemplate {
    private static final long serialVersionUID = 2605665424557979322L;

    @Override
    public void afterPropertiesSet() {
        if (getTransactionManager() == null) {
            setTransactionManager(new MockTransactionManager());
        }
    }

    public <T> T execute(final TransactionCallback<T> action) throws TransactionException {
        final TransactionStatus status = getTransactionManager().getTransaction(this);
        final T result = action.doInTransaction(status);
        getTransactionManager().commit(status);
        return result;
    }
}
