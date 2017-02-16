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

public interface TaskCoordinator {

    String DEFAULT_EXECUTOR = "default";

    SyncTask createTask(ContainerTask<?> parent, Runnable r);
    
    SyncTask createTask(ContainerTask<?> parent, Runnable r, String schedulingHint);
    
    <T> AsyncTask<T> createTask(ContainerTask<?> parent, Async<T> async, Callback<T> cb);
    
    TaskBuilder<BatchTask> createBatch(ContainerTask<?> parent);
    
    TaskBuilder<BatchTask> createBatch();
    
    BatchTask createBatch(ContainerTask<?> parent, Runnable... tasks);

    BatchTask createBatch(Runnable... tasks);

    TaskBuilder<SequenceTask> createSequence(ContainerTask<?> parent);
    
    TaskBuilder<SequenceTask> createSequence();
    
    SequenceTask createSequence(ContainerTask<?> parent, Runnable... tasks);

    void setLoopDelay(long millis);
    
    void schedule(AbstractTask task);
    
    void addDependency(AbstractTask prereq, AbstractTask dependent);

    void markTaskAsCompleted(AbstractTask task);

    void submitToExecutor(String executorPreference, Runnable workToBeDone, AbstractTask owningTask);

    void addOrUpdateExecutor(String executorName, Executor executor);

    void setExecutors(Map<String,Executor> executors);
}
