/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.tasks;

/**
 * This interface is used on objects that are inherently
 * asynchronous (like Mina or Netty library calls). The
 * {@link #supplyAsyncThenAccept(Callback)} method will
 * invoke an async operation that uses the {@link Callback}
 * as the completion and exception handler for the operation.
 *
 * @author Seth
 * @author brozow
 */
public interface Async<T> {
    
    /**
     * <p>submit</p>
     *
     * @param cb a {@link org.opennms.core.tasks.Callback} object.
     * @param <T> a T object.
     */
    void supplyAsyncThenAccept(Callback<T> cb);

}
