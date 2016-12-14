/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * When used in a ThreadPoolExecutor, this queue will block calls to
 * {@link ThreadPoolExecutor#execute(Runnable)} when the queue is full.
 * This is done by overriding calls to {@link LinkedBlockingQueue#offer(Object)}
 * with calls to {@link LinkedBlockingQueue#put(Object)}, but comes with the caveat
 * that executor must be built with <code>corePoolSize == maxPoolSize</code>.
 * In the context of the {@link AsyncDispatcherImpl}, this is an acceptable caveat,
 * since we enforce the matching pool sizes.
 *
 * There are alternative ways of solving this problem, for example we could use a
 * {@link RejectedExecutionHandler} to achieve similar behavior, and allow
 * for <code>corePoolSize < maxPoolSize</code>, but not for <code>corePoolSize==0</code>.
 *
 * For further discussions on this topic see:
 *   http://stackoverflow.com/a/3518588
 *   http://stackoverflow.com/a/32123535
 *
 * If the implementation is changed, make sure that that executor is built accordingly.
 */
public class OfferBlockingQueue<E> extends LinkedBlockingQueue<E> {

    private static final long serialVersionUID = 3311058329865555257L;

    public OfferBlockingQueue(int capacity) {
        super(capacity);
    }

    @Override
    public boolean offer(E e) {
        try {
            put(e);
            return true;
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
