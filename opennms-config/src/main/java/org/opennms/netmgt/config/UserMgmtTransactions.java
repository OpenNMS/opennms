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
package org.opennms.netmgt.config;

import org.springframework.transaction.support.TransactionOperations;

/** Runs work that throws checked exceptions in a transaction, and rethrows them unchanged. */
final class UserMgmtTransactions {

    @FunctionalInterface
    interface Work<T> {
        T run() throws Exception;
    }

    private UserMgmtTransactions() {
    }

    static <T> T inTransaction(final TransactionOperations transactions, final Work<T> work) throws Exception {
        try {
            return transactions.execute(status -> {
                try {
                    return work.run();
                } catch (final RuntimeException e) {
                    throw e;
                } catch (final Exception e) {
                    throw new CheckedWrapper(e);
                }
            });
        } catch (final CheckedWrapper e) {
            throw (Exception) e.getCause();
        }
    }

    /** Carries a checked exception out of the transaction callback; being unchecked, it rolls the transaction back. */
    private static final class CheckedWrapper extends RuntimeException {
        private static final long serialVersionUID = 1L;

        CheckedWrapper(final Exception cause) {
            super(cause);
        }
    }
}
