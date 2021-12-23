/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.netmgmt.alarmd.rest.it;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RetryUtils {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(RetryUtils.class);

    private Logger log = DEFAULT_LOGGER;


    /**
     * Retry the given operation up until the given timeout.  WARNING: the operation is executed synchronously, and
     * hence may well exceed the specified timeout.
     *
     * @param operation the operation to execute on each iteration
     * @param completionPredicate predicate that indicates whether the operation's result completes the retries; when
     *                           false, retry logic will be applied
     * @param iterationDelay the delay, in milliseconds, between individual iterations
     * @param timeout total time, in milliseconds, before counting the operation as timed-out
     * @param initResult initial value to use for result which will be tested, and potentially returned, when the
     *                  operation throws exceptions, followed ultimately by reaching the timeout
     * @param <T>
     * @return
     */
    public <T> T retry(Supplier<T> operation, Predicate<T> completionPredicate, long iterationDelay, long timeout, T initResult)
        throws InterruptedException {

        T result = initResult;

        // Calculate timeout
        long now  = System.nanoTime();
        long start = now;
        long end = start + ( timeout * 1000000L );

        // Prepare storage for exceptions caught be operation
        Exception[] finalExceptionStore = new Exception[1];

        // Initial Attempt
        result = this.safeRunOnce(operation, result, exc -> finalExceptionStore[0] = exc);
        boolean successful = completionPredicate.test(result);

        // Loop until the operation is successful, or timeout is reached.  Note that a timeout of 0 means no retries
        //  will occur - only the initial attempt.
        while (( ! successful ) && ( now < end)) {
            Thread.sleep(iterationDelay);

            result = this.safeRunOnce(operation, result, exc -> finalExceptionStore[0] = exc);
            successful = completionPredicate.test(result);

            now = System.nanoTime();
        }

        if ((! successful) && (finalExceptionStore[0] != null)) {
            this.log.warn("RETRY UTIL: operation timed out: final exception (may not be from the final attempt)",
                          finalExceptionStore[0]);
        }

        return result;
    }

    private <T> T safeRunOnce(Supplier<T> operation, T defaultResult, Consumer<Exception> onException) {
        T result = defaultResult;
        try {
            result = operation.get();
        } catch (Exception exc) {
            this.log.debug("RETRY util: operation threw exception", exc);
            onException.accept(exc);
        }

        return result;
    }
}
