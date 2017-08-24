/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * TaskCoordinator
 *
 * @author brozow
 * @version $Id: $
 */
public interface TaskCoordinator {

    /** Constant <code>DEFAULT_EXECUTOR="default"</code> */
    public static final String DEFAULT_EXECUTOR = "default";

    /**
     * <p>createTask</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param r a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.SyncTask} object.
     */
    SyncTask createTask(ContainerTask<?> parent, Runnable r);
    
    /**
     * <p>createTask</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param r a {@link java.lang.Runnable} object.
     * @param schedulingHint a {@link java.lang.String} object.
     * @return a {@link org.opennms.core.tasks.SyncTask} object.
     */
    SyncTask createTask(ContainerTask<?> parent, Runnable r, String schedulingHint);
    
    /**
     * <p>createTask</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param async a {@link org.opennms.core.tasks.Async} object.
     * @param cb a {@link org.opennms.core.tasks.Callback} object.
     * @param <T> a T object.
     * @return a {@link org.opennms.core.tasks.AsyncTask} object.
     */
    <T> AsyncTask<T> createTask(ContainerTask<?> parent, Async<T> async, Callback<T> cb);
    

    /**
     * <p>createBatch</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    TaskBuilder<BatchTask> createBatch(ContainerTask<?> parent);
    
    /**
     * <p>createBatch</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    TaskBuilder<BatchTask> createBatch();
    
    /**
     * <p>createBatch</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param tasks a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.BatchTask} object.
     */
    BatchTask createBatch(ContainerTask<?> parent, Runnable... tasks);

    
    /**
     * <p>createBatch</p>
     *
     * @param tasks a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.BatchTask} object.
     */
    BatchTask createBatch(Runnable... tasks);

    
    /**
     * <p>createSequence</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    TaskBuilder<SequenceTask> createSequence(ContainerTask<?> parent);
    
    /**
     * <p>createSequence</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    TaskBuilder<SequenceTask> createSequence();
    
    /**
     * <p>createSequence</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param tasks a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.SequenceTask} object.
     */
    SequenceTask createSequence(ContainerTask<?> parent, Runnable... tasks);

    /**
     * <p>setLoopDelay</p>
     *
     * @param millis a long.
     */
    void setLoopDelay(long millis);
    
    /**
     * <p>schedule</p>
     *
     * @param task a {@link org.opennms.core.tasks.AbstractTask} object.
     */
    void schedule(AbstractTask task);
    
    /**
     * <p>addDependency</p>
     *
     * @param prereq a {@link org.opennms.core.tasks.AbstractTask} object.
     * @param dependent a {@link org.opennms.core.tasks.AbstractTask} object.
     */
    void addDependency(AbstractTask prereq, AbstractTask dependent);

    void markTaskAsCompleted(AbstractTask task);

    void submitToExecutor(String executorPreference, Runnable workToBeDone, AbstractTask owningTask);

    /**
     * <p>addOrUpdateExecutor</p>
     *
     * @param executorName a {@link java.lang.String} object.
     * @param executor a {@link java.util.concurrent.Executor} object.
     */
    void addOrUpdateExecutor(String executorName, Executor executor);

    /**
     * <p>setExecutors</p>
     *
     * @param executors a {@link java.util.Map} object.
     */
    void setExecutors(Map<String,Executor> executors);
}
