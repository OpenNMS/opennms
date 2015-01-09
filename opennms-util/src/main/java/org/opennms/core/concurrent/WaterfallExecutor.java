/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.concurrent;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


/**
 * @author Seth
 */
public abstract class WaterfallExecutor {
    public static void waterfall(Iterable<ExecutorService> executors, Callable<Callable<?>> callable) throws InterruptedException, ExecutionException {
        waterfall(executors.iterator(), callable);
    }

    /**
     * This function recursively calls the {@link WaterfallCallable} tasks with the given chain of ExecutorServices.
     */
    @SuppressWarnings("unchecked")
    private static void waterfall(Iterator<ExecutorService> executors, Callable<Callable<?>> callable) throws InterruptedException, ExecutionException {
        // Fetch the next ExecutorService
        ExecutorService executor = null;
        try {
            executor = executors.next();
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Not enough executors to service this Future task: " + callable);
        }

        if (executor == null) {
            throw new IllegalStateException("Not enough executors to service this Future task: " + callable);
        }

        // Submit the task to the current ExecutorService
        Future<Callable<?>> task = executor.submit(callable);

        Callable value = task.get();
        if (value != null) {
            // Send the return value to the next ExecutorService
            waterfall(executors, value);
        }
        // The {@link WaterfallCallable} returned null; this terminates the chain of execution
    }
}
