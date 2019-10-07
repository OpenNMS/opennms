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

    /**
     * Invokes the given supplier within the context of a read-only transaction.
     *
     * @param supplier supplier to invoke
     * @param <V> type returned by the supplier
     * @return value returned by the supplier
     */
    <V> V withReadOnlyTransaction(Supplier<V> supplier);

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

}
