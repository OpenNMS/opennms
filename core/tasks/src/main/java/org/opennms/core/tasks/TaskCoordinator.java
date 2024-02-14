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
