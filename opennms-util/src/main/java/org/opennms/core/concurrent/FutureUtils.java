/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2021 The OpenNMS Group, Inc.
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
 *******************************************************************************/

package org.opennms.core.concurrent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Provides utility functions for creating and combining completion stages.
 * <p>
 * In general, completable futures do not interrupt ongoing executions when cancelled
 * (cf. {@link CompletableFuture#cancel(boolean)}). This class provides utility methods for creating completion
 * stages that cancel ongoing computations in case of timeouts. Depending on the supplied executor service
 * these cancellations will cause the interruption of ongoing executions.
 * <p>
 * <b>Note:</b> Do not use a {@link java.util.concurrent.ForkJoinTask} when construction these completion stages
 * because {@link java.util.concurrent.ForkJoinTask#cancel(boolean)} does not interrupt ongoing executions.
 * <p>
 * Other utility methods allow to collect the results of lists of completion stages into single list valued
 * completion stage.
 */
public class FutureUtils {

    private static Timer timer = new Timer("opennms.FutureUtils", true);

    /**
     * Returns a completion stage that is guaranteed to complete.
     * <p>
     * It completes with the value returned by the given callable or the supplied default
     * value in case of a timeout. In case the invocation of the callable throws
     * an exception it completes exceptionally.
     * <p>
     * Note: The execution of the given callable is cancelled on timeout.
     *
     * @param executorService The executor service for executing the callable.
     */
    public static <T> CompletionStage<T> completionStageWithDefaultOnTimeout(
            Callable<T> callable,
            Duration timeout,
            Supplier<T> defaultValue,
            ExecutorService executorService
    ) {
        return completionStage(callable, timeout, cf -> cf.complete(defaultValue.get()), executorService);
    }

    /**
     * Returns a completion stage that is guaranteed to complete.
     * <p>
     * It completes with the value returned by the given callable. In case of a timeout or in case that
     * the invocation of the callable throws an exception is completes exceptionally.
     * <p>
     * Note: The execution of the given callable is cancelled on timeout.
     *
     * @param executorService The executor service for executing the callable.
     */
    public static <T, EX extends Throwable> CompletionStage<T> completionStageWithTimeoutException(
            Callable<T> callable,
            Duration timeout,
            Supplier<EX> timeoutException,
            ExecutorService executorService
    ) {
        return completionStage(callable, timeout, cf -> cf.completeExceptionally(timeoutException.get()), executorService);
    }

    /**
     * Returns a completion stage that is guaranteed to complete.
     * <p>
     * It completes with the value returned by the given callable. In case the invocation of the callable throws
     * an exception it completes exceptionally. In case of a timeout the given callback must
     * complete the completable future either with a value or exceptionally.
     * <p>
     * Note: The execution of the given callable is cancelled on timeout.
     *
     * @param executorService The executor service for executing the callable.
     */
    public static <T> CompletionStage<T> completionStage(
            Callable<T> callable,
            Duration timeout,
            Consumer<CompletableFuture<T>> onTimeout,
            ExecutorService executorService
    ) {
        var result = new CompletableFuture<T>();
        var future = executorService.submit(() -> {
            try {
                result.complete(callable.call());
            } catch (Throwable e) {
                result.completeExceptionally(e);
            }
        });
        var timerTask = new TimerTask() {
            @Override
            public void run() {
                // First, allow the 'onTimeout' callback to complete the result with a default value or a timeout exception
                onTimeout.accept(result);
                // ... then cancel the underlying future (which in turn may trigger an interrupted exception of the
                // submitted callable.call() invocation. The interrupted exception gets ignored because the result
                // was already completed.
                future.cancel(true);
            }
        };
        timer.schedule(timerTask, timeout.toMillis());
        result.thenRun(timerTask::cancel);
        return result;
    }

    /**
     * Collects the results of the futures into a list. The order of results in the list corresponds to the order
     * of the given futures.
     */
    public static <T> CompletionStage<List<T>> sequence(List<CompletionStage<T>> cfs) {
        return traverse(cfs, Function.identity());
    }

    /**
     * Converts a list of values into futures and collects their results. The order of the results in the list
     * corresponds to the order of the given values.
     * <p>
     * All futures are created initially in order to start their execution in parallel.
     */
    public static <U, V> CompletionStage<List<V>> traverse(
            List<U> us,
            Function<U, CompletionStage<V>> func
    ) {
        var futures = us.stream().map(func).collect(Collectors.toList());
        CompletableFuture<List<V>> result = CompletableFuture.completedFuture(new ArrayList<>());
        for (var f: futures) {
            result = result.thenCombine(f, (list, item) -> { list.add(item); return list; });
        }
        return result;
    }

}
