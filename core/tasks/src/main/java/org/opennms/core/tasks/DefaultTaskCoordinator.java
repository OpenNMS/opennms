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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.opennms.core.concurrent.LogPreservingThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * This {@link DefaultTaskCoordinator} class provides utility methods to construct
 * and schedule hierarchies of {@link Tasks}.
 * 
 * @author brozow
 */
public class DefaultTaskCoordinator implements TaskCoordinator, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskCoordinator.class);

    /**
     * This interface is used as a marker for {@link Runnable} tasks that
     * are intended to be enqueued on the {@link RunnableActor} thread.
     */
    interface SerialRunnable extends Runnable {}

    /**
     * <p>This {@link Executor} handles all of the task dependency work to reduce the 
     * need for synchronization. The single thread:</p>
     * 
     * <ul>
     * <li>Processes the completion queue</li>
     * <li>Updates dependencies due to completing tasks</li>
     * <li>Schedules tasks that must be run due to completing dependencies</li>
     * <li>Schedules the adding of dependencies</li>
     * </ul>
     *
     * @author brozow
     */
    private final Executor m_actorExecutor;

    private final ConcurrentHashMap<String, Executor> m_taskExecutors = new ConcurrentHashMap<String, Executor>();

    private String m_defaultExecutorName = TaskCoordinator.DEFAULT_EXECUTOR;

    private long m_loopDelay = 0;

    /**
     * <p>Constructor for DefaultTaskCoordinator.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param defaultExecutor a {@link java.util.concurrent.Executor} object.
     */
    public DefaultTaskCoordinator(String name) {
        // Create a new single-threaded actor executor
        m_actorExecutor = Executors.newSingleThreadExecutor(
            new LogPreservingThreadFactory(name+"-TaskScheduler", 1)
        );

        // By default, add one single-threaded task executor to the coordinator
        addOrUpdateExecutor(
            m_defaultExecutorName,
            Executors.newSingleThreadExecutor(
                new LogPreservingThreadFactory(m_defaultExecutorName, 1)
            )
        );
    }

    /**
     * <p>setDefaultExecutor</p>
     *
     * @param executorName a {@link java.lang.String} object.
     */
    public final void setDefaultExecutor(String executorName) {
        m_defaultExecutorName = executorName;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_defaultExecutorName, "defaultExecutor must be set");
        Assert.notNull(getExecutor(m_defaultExecutorName), "defaultExecutor must be set to the name of an added executor");
    }
    
    /**
     * <p>createTask</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param r a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.SyncTask} object.
     */
    @Override
    public SyncTask createTask(ContainerTask<?> parent, Runnable r) {
        return new SyncTask(this, parent, r);
    }
    
    /**
     * <p>createTask</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param r a {@link java.lang.Runnable} object.
     * @param schedulingHint a {@link java.lang.String} object.
     * @return a {@link org.opennms.core.tasks.SyncTask} object.
     */
    @Override
    public SyncTask createTask(ContainerTask<?> parent, Runnable r, String schedulingHint) {
        return new SyncTask(this, parent, r, schedulingHint);
    }
    
    /**
     * <p>createTask</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param async a {@link org.opennms.core.tasks.Async} object.
     * @param cb a {@link org.opennms.core.tasks.Callback} object.
     * @param <T> a T object.
     * @return a {@link org.opennms.core.tasks.AsyncTask} object.
     */
    @Override
    public <T> AsyncTask<T> createTask(ContainerTask<?> parent, Async<T> async, Callback<T> cb) {
        return new AsyncTask<T>(this, parent, async, cb);
    }

    /**
     * <p>createBatch</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    @Override
    public TaskBuilder<BatchTask> createBatch(ContainerTask<?> parent) {
        return new TaskBuilder<BatchTask>(new BatchTask(this, parent));
    }
    
    /**
     * <p>createBatch</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    @Override
    public TaskBuilder<BatchTask> createBatch() {
        return createBatch((ContainerTask<?>)null);
    }
    
    /**
     * <p>createBatch</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param tasks a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.BatchTask} object.
     */
    @Override
    public BatchTask createBatch(ContainerTask<?> parent, Runnable... tasks) {
        return createBatch(parent).add(tasks).get(parent);
    }

    
    /**
     * <p>createBatch</p>
     *
     * @param tasks a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.BatchTask} object.
     */
    @Override
    public BatchTask createBatch(Runnable... tasks) {
        return createBatch().add(tasks).get();
    }

    
    /**
     * <p>createSequence</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    @Override
    public TaskBuilder<SequenceTask> createSequence(ContainerTask<?> parent) {
        return new TaskBuilder<SequenceTask>(new SequenceTask(this, parent));
    }
    
    /**
     * <p>createSequence</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    @Override
    public TaskBuilder<SequenceTask> createSequence() {
        return createSequence((ContainerTask<?>)null);
    }
    
    /**
     * <p>createSequence</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param tasks a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.SequenceTask} object.
     */
    @Override
    public SequenceTask createSequence(ContainerTask<?> parent, Runnable... tasks) {
        return createSequence(parent).add(tasks).get(parent);
    }

    /**
     * <p>setLoopDelay</p>
     *
     * @param millis a long.
     */
    @Override
    public final void setLoopDelay(long millis) {
        m_loopDelay = millis;
    }
    
    /**
     * <p>schedule</p>
     *
     * @param task a {@link org.opennms.core.tasks.AbstractTask} object.
     */
    @Override
    public void schedule(final AbstractTask task) {
        onProcessorThread(scheduler(task));
    }
    
    /**
     * <p>addDependency</p>
     *
     * @param prereq a {@link org.opennms.core.tasks.AbstractTask} object.
     * @param dependent a {@link org.opennms.core.tasks.AbstractTask} object.
     */
    @Override
    public void addDependency(AbstractTask prereq, AbstractTask dependent) {
        // this is only needed when add dependencies while running
        dependent.incrPendingPrereqCount();
        onProcessorThread(dependencyAdder(prereq, dependent));
    }

    void onProcessorThread(final SerialRunnable r) {
        // If there's a delay set for testing, run the task
        // and then sleep for the delay
        CompletableFuture<Void> future = null;
        if (m_loopDelay > 0) {
            future = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    r.run();
                    try {
                        Thread.sleep(m_loopDelay);
                    } catch (InterruptedException e) {}
                }
                
            }, m_actorExecutor);
        } else {
            future = CompletableFuture.runAsync(r, m_actorExecutor);
        }
        future.exceptionally(e -> {
            LOG.warn("Unexpected exception during actor runnable: " + e.getMessage(), e);
            return null;
        });
    }


    private static SerialRunnable scheduler(final AbstractTask task) {
        return new SerialRunnable() {
            @Override
            public void run() {
                task.scheduled();
                task.submitIfReady(); 
            }
            @Override
            public String toString() {
                return String.format("schedule(%s)", task);
            }
        };
    }
    
    private static SerialRunnable taskCompleter(final AbstractTask task) {
        return new SerialRunnable() {
            @Override
            public void run() {
                notifyDependents(task);
            }
            @Override
            public String toString() {
                return String.format("notifyDependents(%s)", task);
            }
        };
    }
    
    
    private static void notifyDependents(AbstractTask task) {
        //LOG.debug("Task {} completed!", task);
        task.onComplete();

        final Set<AbstractTask> dependents = task.getDependents();
        for(AbstractTask dependent : dependents) {
            dependent.doCompletePrerequisite(task);
            /*
            if (LOG.isDebugEnabled()) {
                if (dependent.isReady()) {
                    LOG.debug("Task {} {} ready.", dependent, dependent.isReady() ? "is" : "is not");
                }
            }
            */
            dependent.submitIfReady();
        }

        //LOG.debug("CLEAN: removing dependents of {}", task);
        task.clearDependents();
    }

    /**
     * The returns a runnable that is run on the taskCoordinator thread.. This is 
     * done to keep the Task data structures thread safe.
     */
    private static SerialRunnable dependencyAdder(final AbstractTask prereq, final AbstractTask dependent) {
        Assert.notNull(prereq, "prereq must not be null");
        Assert.notNull(dependent, "dependent must not be null");
        return new SerialRunnable() {
            @Override
            public void run() {
                prereq.doAddDependent(dependent);
                dependent.doAddPrerequisite(prereq);
                dependent.decrPendingPrereqCount();

                /**
                 *  the prereq task may have completed between the time this adder was enqueued
                 *  and the time we got here.  In this case there will be no tasks to kick this
                 *  one off... so check it here. 
                 */
                dependent.submitIfReady();
            }
            @Override
            public String toString() {
                return String.format("%s.addPrerequisite(%s)", dependent, prereq);
            }
        };
    }
    
    
    private final Executor getExecutor(String name) {
        Executor executor = m_taskExecutors.get(name);
        if (executor == null) {
            Executor defaultExecutor = m_taskExecutors.get(m_defaultExecutorName);
            if (defaultExecutor == null) {
                throw new IllegalStateException("No default executor in " + getClass().getName());
            } else {
                return defaultExecutor;
            }
        } else {
            //LOG.debug("Using executor {}: {}", name, executor);
            return executor;
        }
    }
    
    @Override
    public void markTaskAsCompleted(AbstractTask task) {
        onProcessorThread(taskCompleter(task));
    }

    @Override
    public void submitToExecutor(String executorPreference, Runnable workToBeDone, AbstractTask owningTask) {
        CompletableFuture
            // Run the work on the preferred executor
            .runAsync(workToBeDone, getExecutor(executorPreference))
            // Log any uncaught exceptions from the task execution
            .exceptionally(e -> {
                LOG.warn("Unexpected exception during task execution: " + e.getMessage(), e);
                return null;
            })
            // Then run the completer on the actor executor
            .thenRunAsync(taskCompleter(owningTask), m_actorExecutor)
            // Log any uncaught exceptions from the task completer
            .exceptionally(e -> {
                LOG.warn("Unexpected exception during task completion: " + e.getMessage(), e);
                return null;
            });
    }

    /**
     * <p>addExecutor</p>
     *
     * @param executorName a {@link java.lang.String} object.
     * @param executor a {@link java.util.concurrent.Executor} object.
     */
    @Override
    public final void addOrUpdateExecutor(String executorName, Executor executor) {
        Executor service = m_taskExecutors.put(executorName, executor);
        if (service != null) {
            LOG.info("Replacing executor {} with {}", executorName, executor);
        }
    }

    /**
     * <p>setExecutors</p>
     *
     * @param executors a {@link java.util.Map} object.
     */
    @Override
    public final void setExecutors(Map<String,Executor> executors) {
        m_taskExecutors.clear();
        for (Map.Entry<String, Executor> e : executors.entrySet()) {
            addOrUpdateExecutor(e.getKey(), e.getValue());
        }
    }

}
