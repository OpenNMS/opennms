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
package org.opennms.netmgt.dao.api;

import java.util.function.Supplier;

/**
 * Utility functions for dealing with Hibernate session and transactions.
 *
 * These functions were added to this interface to help make them accessible
 * to bundles running in the OSGi container without having to deal with class-path issues
 * related to Spring & Hibernate.
 *
 * @author jwhite
 */
public interface SessionUtils {

    /**
     * Invoked the given supplier within the context of a transaction.
     *
     * @param supplier supplier to invoke
     * @param <V> type returned by the supplier
     * @return value returned by the supplier
     */
    <V> V withTransaction(Supplier<V> supplier);

    default void withTransaction(Runnable runnable) {
        this.withTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Invokes the given supplier within the context of a read-only transaction.
     *
     * @param supplier supplier to invoke
     * @param <V> type returned by the supplier
     * @return value returned by the supplier
     */
    <V> V withReadOnlyTransaction(Supplier<V> supplier);

    default void withReadOnlyTransaction(Runnable runnable) {
        this.withReadOnlyTransaction(() -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Converts the flush mode for the current session factory to MANUAL
     * for the duration of the call to the given supplier.
     *
     * The flush mode is reverted to it's previous value after the call.
     *
     * @param supplier supplier to invoke
     * @param <V> type returned by the supplier
     * @return value returned by the supplier
     */
    <V> V withManualFlush(Supplier<V> supplier);

    default void withManualFlush(Runnable runnable) {
        this.withManualFlush(() -> {
            runnable.run();
            return null;
        });
    }

}
